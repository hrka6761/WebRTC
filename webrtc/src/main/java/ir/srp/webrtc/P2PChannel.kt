package ir.srp.webrtc

import android.app.Application
import ir.srp.webrtc.data_converters.ChannelData
import ir.srp.webrtc.exceptions.IllegalUrlException
import ir.srp.webrtc.exceptions.NoHandShakeException
import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.models.DataType
import ir.srp.webrtc.utils.Validation.isValidSignalingServerUrl
import ir.srp.webrtc.webSocket.WebSocketClient
import ir.srp.webrtc.webSocket.WebSocketListener
import ir.srp.webrtc.observers.AnswerSdpObserver
import ir.srp.webrtc.observers.DataChannelObserver
import ir.srp.webrtc.observers.PeerConnectionObserver
import ir.srp.webrtc.observers.CallSdpObserver
import ir.srp.webrtc.observers.PeerSdpObserver
import ir.srp.webrtc.data_converters.JsonConverter.convertJsonStringToObject
import ir.srp.webrtc.data_converters.JsonConverter.convertObjectToJsonString
import ir.srp.webrtc.exceptions.NoSignallingConnectionException
import ir.srp.webrtc.models.P2PConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.IOException
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.VideoDecoderFactory
import org.webrtc.VideoEncoderFactory
import kotlin.jvm.Throws

class P2PChannel private constructor(
    private var options: PeerConnectionFactory.InitializationOptions,
    private var videoEncoderFactory: VideoEncoderFactory,
    private var videoDecoderFactory: VideoDecoderFactory,
    private var iceServers: List<IceServer>,
    private var mediaConstraints: MediaConstraints,
    private val signalingServerUrl: String,
    private var username: String,
    private var eventsListener: ChannelEventsListener?,
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private lateinit var peerConnectionObserver: PeerConnectionObserver
    private lateinit var dataChannelObserver: DataChannelObserver
    private lateinit var callSdpObserver: CallSdpObserver
    private lateinit var answerSdpObserver: AnswerSdpObserver
    private lateinit var peerSdpObserver: PeerSdpObserver
    private lateinit var signalingServerConnection: WebSocketClient
    private lateinit var signalingServerListener: WebSocketListener
    private var dataChannel: DataChannel? = null
    private var target: String? = null
    private var isSignallingConnected = false
    private var doHandshake: Boolean = false
    private var isChannelReady: Boolean = false


    companion object {

        class Builder(
            private val context: Application,
            private val signalingServerUrl: String,
            private val username: String,
            private val iceServers: List<IceServer>,
        ) {

            private lateinit var p2PChannel: P2PChannel
            private var mOptions = getDefaultInitializationOptions()
            private val eglBaseContext = EglBase.create().eglBaseContext
            private var mVideoEncoderFactory = getDefaultVideoEncoderFactory()
            private var mVideoDecoderFactory = getDefaultVideoDecoderFactory()
            private var mMediaConstraints = getDefaultMediaConstraints()
            private var mEventsListener: ChannelEventsListener? = null
            private var isPeerConnectionFactoryInit = false


            private fun getDefaultInitializationOptions() = PeerConnectionFactory
                .InitializationOptions
                .builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions()

            private fun getDefaultVideoEncoderFactory(): VideoEncoderFactory =
                DefaultVideoEncoderFactory(
                    eglBaseContext,
                    true,
                    true
                )

            private fun getDefaultVideoDecoderFactory(): VideoDecoderFactory =
                DefaultVideoDecoderFactory(eglBaseContext)

            private fun getDefaultMediaConstraints() = MediaConstraints()


            fun setEventsListener(channelEventsListener: ChannelEventsListener): Builder {
                mEventsListener = channelEventsListener

                return this
            }

            fun setInitializationOptions(options: PeerConnectionFactory.InitializationOptions): Builder {
                mOptions = options

                return this
            }

            fun setVideoEncoderFactory(videoEncoderFactory: VideoEncoderFactory): Builder {
                mVideoEncoderFactory = videoEncoderFactory

                return this
            }

            fun setVideoEncoderFactory(videoDecoderFactory: VideoDecoderFactory): Builder {
                mVideoDecoderFactory = videoDecoderFactory

                return this
            }

            fun setMediaConstraints(mediaConstraints: MediaConstraints): Builder {
                mMediaConstraints = mediaConstraints

                return this
            }

            fun build(): P2PChannel {
                p2PChannel = P2PChannel(
                    mOptions,
                    mVideoEncoderFactory,
                    mVideoDecoderFactory,
                    iceServers,
                    mMediaConstraints,
                    signalingServerUrl,
                    username,
                    mEventsListener
                )

                if (!isPeerConnectionFactoryInit) {
                    p2PChannel.initializePeerConnectionFactory()
                    isPeerConnectionFactoryInit = true
                }

                return p2PChannel
            }
        }
    }


    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initialize(options)
    }

    private fun initializePeerConnection() {
        peerConnectionObserver = PeerConnectionObserver(
            onProvideDataChannel = { dataChannel ->
                this.dataChannel = dataChannel
                doHandshake = true
                eventsListener?.onCreateP2PConnection()
                isChannelReady = true
            },
            onProvideIceCandidate = { iceCandidate ->
                signalingServerConnection.sendData(
                    DataModel(
                        type = DataType.Ice,
                        username = username,
                        target = target,
                        data = iceCandidate?.let { convertObjectToJsonString(it) }
                    )
                )
            },
            onConnectionStateChange = { state ->
                eventsListener?.onP2PConnectionStateChange(state)
                if (state == P2PConnectionState.DISCONNECTED.name || state == P2PConnectionState.CLOSED.name)
                    eventsListener?.onDestroyP2PConnection()
            }
        )
        createPeerConnectionFactory()
        createPeerConnection()
        createDataChannel()
    }

    private fun initializePeerOnSignalingServer() {
        signalingServerConnection.sendData(
            DataModel(
                type = DataType.SignIn,
                username = username,
                target = null,
                data = null
            )
        )
    }

    private fun createPeerConnectionFactory() {
        peerConnectionFactory = PeerConnectionFactory
            .builder()
            .setVideoEncoderFactory(videoEncoderFactory)
            .setVideoDecoderFactory(videoDecoderFactory)
            .setOptions(PeerConnectionFactory
                .Options().apply {
                    disableEncryption = false
                    disableNetworkMonitor = false
                }
            )
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection() {
        peerConnection =
            peerConnectionFactory.createPeerConnection(iceServers, peerConnectionObserver)
    }

    private fun createDataChannel() {
        val initDataChannel = DataChannel.Init()
        val dataChannel = peerConnection?.createDataChannel("dataChannelLabel", initDataChannel)
        dataChannelObserver = DataChannelObserver(eventsListener)
        dataChannel?.registerObserver(dataChannelObserver)
    }

    private fun processReceivedSignalingMessage(message: DataModel) {
        when (message.type) {
            DataType.Handshake -> {
                this.target = message.username
                call(target = message.username)
            }

            DataType.Call -> {
                setPeerSdp(
                    SessionDescription(
                        SessionDescription.Type.OFFER,
                        message.data.toString()
                    )
                )

                answer(message.username)
            }

            DataType.Answer -> {
                setPeerSdp(
                    SessionDescription(
                        SessionDescription.Type.ANSWER,
                        message.data.toString()
                    )
                )
            }

            DataType.Ice -> {
                val iceCandidate = convertJsonStringToObject(
                    message.data.toString(),
                    IceCandidate::class.java
                ) as IceCandidate

                peerConnection?.addIceCandidate(iceCandidate)
            }

            else -> Unit
        }
    }

    private fun setPeerSdp(peerSdp: SessionDescription) {
        peerSdpObserver = PeerSdpObserver()
        peerConnection?.setRemoteDescription(peerSdpObserver, peerSdp)
    }

    private fun call(target: String) {
        if (!this::callSdpObserver.isInitialized) {
            peerConnection?.let {
                callSdpObserver =
                    CallSdpObserver(it, signalingServerConnection, username, target)
            }
        }

        peerConnection?.createOffer(callSdpObserver, mediaConstraints)
    }

    private fun answer(target: String) {
        if (!this::answerSdpObserver.isInitialized) {
            peerConnection?.let {
                answerSdpObserver =
                    AnswerSdpObserver(it, signalingServerConnection, username, target)
            }
        }

        peerConnection?.createAnswer(answerSdpObserver, mediaConstraints)
    }


    @Throws(IllegalArgumentException::class)
    fun createSignalingConnection() {
        if (!isValidSignalingServerUrl(signalingServerUrl))
            throw IllegalUrlException()

        signalingServerListener = WebSocketListener(
            onOpenCallback = { response ->
                isSignallingConnected = true
                initializePeerConnection()
                initializePeerOnSignalingServer()
                eventsListener?.onSuccessSignalingServerConnection(signalingServerConnection)
            },
            onClosedCallback = { code, reason ->
                eventsListener?.onCLoseSignalingServerConnection(code, reason)
            },
            onClosingCallback = { code, reason -> },
            onFailureCallback = { throwable, response ->
                eventsListener?.onFailedSignalingServerConnection(throwable)
            },
            onMessageCallback = { message ->
                eventsListener?.onReceiveSignalingData(message)
                processReceivedSignalingMessage(message)
            }
        )
        signalingServerConnection = WebSocketClient(signalingServerUrl, signalingServerListener)
        signalingServerConnection.createConnection()
    }

    fun handshake(target: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isSignallingConnected) {
                this@P2PChannel.target = target

                signalingServerConnection.sendData(
                    DataModel(
                        type = DataType.Handshake,
                        username = username,
                        target = target,
                        data = null
                    )
                )

                doHandshake = true
            } else
                throw NoSignallingConnectionException()
        }
    }

    @Throws(IOException::class)
    fun sendData(channelData: ChannelData) {
        if (doHandshake)
            CoroutineScope(Dispatchers.IO).launch {
                for (data in channelData())
                    dataChannel?.send(data)
            }
        else
            throw NoHandShakeException()
    }

    fun getChannelState() = dataChannel?.state()

    fun closeChannel() {
        dataChannel?.close()
        peerConnection?.close()
        doHandshake = false
        isChannelReady = false
    }
}