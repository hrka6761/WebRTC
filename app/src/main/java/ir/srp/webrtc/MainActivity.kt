package ir.srp.webrtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import ir.srp.webrtc.data_converters.TextData
import ir.srp.webrtc.databinding.ActivityMainBinding
import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.models.IceServerModel
import ir.srp.webrtc.utils.IceServerBuilder.createListOfIceServers
import ir.srp.webrtc.webSocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "HAMIDREZA"
    private val SIGNALING_SERVER_URL = "ws://srp-rasad.ir:13676"
    private lateinit var binding: ActivityMainBinding
    private lateinit var channelBuilder: P2PChannel.Companion.Builder
    private lateinit var p2PChannel: P2PChannel
    private val receivedMessage = StringBuffer("")
    private var signalingServerConnection: WebSocketClient? = null
    private var isBuiltChannel = false
    private var isP2PConnectionCreated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize() {
        initSignalingServerConnectionButton()
        initP2PConnectionButton()
        initSignInButton()
        initHandShakeButton()
        initSendMessageButton()
    }

    private fun initP2PConnectionButton() {
        binding.webrtcConnectionBtn.setOnClickListener {
            if (isP2PConnectionCreated)
                displayDialog(
                    "P2P connection",
                    "Are you sure you want to disconnect from the peer ?",
                    {
                        p2PChannel.closeChannel()
                    },
                    {}
                )
        }
    }

    private fun initSignalingServerConnectionButton() {
        binding.signalingServerConnectionBtn.setOnClickListener {
            if (signalingServerConnection != null)
                displayDialog(
                    "Signaling server connection",
                    "Are you sure you want to disconnect from the signaling server ?",
                    { signalingServerConnection?.removeConnection() },
                    {}
                )
        }
    }

    private fun initSignInButton() {
        binding.signInBtn.setOnClickListener {
            val username = binding.usernameEdt.text.toString()
            if (username.isNotEmpty()) {
                binding.usernameEdtl.isEnabled = false
                binding.signInBtn.isEnabled = false
                if (!isBuiltChannel) {
                    initP2PChannelBuilder(username)
                    isBuiltChannel = true
                }
                p2PChannel = channelBuilder.build()
                p2PChannel.createSignalingConnection()
            } else
                warning("Please enter a username")
        }
    }

    private fun initHandShakeButton() {
        binding.handshakeBtn.setOnClickListener {
            val target = binding.targetUsernameEdt.text.toString()
            if (target.isNotEmpty())
                p2PChannel.handshake(target)
            else
                warning("Please enter a target")
        }
    }

    private fun initSendMessageButton() {
        binding.sendMessageBtn.setOnClickListener {
            val message = binding.messageEdt.text.toString()
            if (message.isNotEmpty() && isP2PConnectionCreated)
                p2PChannel.sendData(TextData(message))
            else
                warning("Please enter a message")
        }
    }

    private fun initP2PChannelBuilder(username: String) {
        channelBuilder = P2PChannel.Companion.Builder(
            application,
            SIGNALING_SERVER_URL,
            username,
            createListOfIceServers(
                IceServerModel(
                    "turn:openrelay.metered.ca:80",
                    "openrelayproject",
                    "openrelayproject"
                ),
                IceServerModel("stun:stun.l.google.com:19302"),
                IceServerModel("stun:stun.avigora.com:3478"),
                IceServerModel("stun:stun.actionvoip.com:3478"),
                IceServerModel("stun:stun.2talk.co.nz:3478"),
                IceServerModel("stun:iphone-stun.strato-iphone.de:3478"),
                IceServerModel("stun:s1.taraba.net:3478"),
                IceServerModel("stun:stun.12connect.com:3478"),
            )
        ).setEventsListener(ChannelEventListener())
    }

    private fun warning(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(binding.root, message, duration).show()
    }

    private fun displayDialog(
        title: String,
        message: String,
        positiveCallback: () -> Unit,
        negativeCallback: () -> Unit,
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("ok") { dialog, _ ->
                positiveCallback()
                dialog.dismiss()
            }
            .setNegativeButton("cancel") { dialog, _ ->
                negativeCallback()
                dialog.dismiss()
            }
            .show()
    }

    private fun setButtonBackgroundColor(btn: MaterialButton, colorId: Int) {
        btn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, colorId)
    }


    private inner class ChannelEventListener : ChannelEventsListener {
        override fun onSuccessSignalingServerConnection(webSocket: WebSocketClient) {
            signalingServerConnection = webSocket
            setButtonBackgroundColor(binding.signalingServerConnectionBtn, R.color.green)
            binding.targetUsernameEdtl.isEnabled = true
            binding.handshakeBtn.isEnabled = true
        }

        override fun onFailedSignalingServerConnection(t: Throwable) {
            setButtonBackgroundColor(binding.signalingServerConnectionBtn, R.color.red)
            if (!isP2PConnectionCreated) {
                binding.usernameEdtl.isEnabled = true
                binding.signInBtn.isEnabled = true
            }
            binding.targetUsernameEdtl.isEnabled = false
            binding.handshakeBtn.isEnabled = false
            warning(t.message.toString())
            signalingServerConnection = null

        }

        override fun onCLoseSignalingServerConnection(code: Int, reason: String) {
            setButtonBackgroundColor(binding.signalingServerConnectionBtn, R.color.red)
            if (!isP2PConnectionCreated) {
                binding.usernameEdtl.isEnabled = true
                binding.signInBtn.isEnabled = true
            }
            binding.targetUsernameEdtl.isEnabled = false
            binding.handshakeBtn.isEnabled = false

            if (reason.isNotEmpty())
                warning(reason)
            else
                warning("You disconnect from signaling server.")

            signalingServerConnection = null
        }

        override fun onCreateP2PConnection() {
            isP2PConnectionCreated = true
            setButtonBackgroundColor(binding.webrtcConnectionBtn, R.color.green)
            binding.handshakeBtn.isEnabled = false
            binding.targetUsernameEdtl.isEnabled = false
            binding.messageEdt.isEnabled = true
            binding.sendMessageBtn.isEnabled = true

            signalingServerConnection?.removeConnection()
        }

        override fun onDestroyP2PConnection() {
            isP2PConnectionCreated = false
            setButtonBackgroundColor(binding.webrtcConnectionBtn, R.color.red)
            if (signalingServerConnection == null) {
                binding.usernameEdtl.isEnabled = true
                binding.signInBtn.isEnabled = true
            } else {
                binding.targetUsernameEdtl.isEnabled = true
                binding.handshakeBtn.isEnabled = true
            }
            binding.messageEdt.isEnabled = false
            binding.sendMessageBtn.isEnabled = false
        }

        override fun onP2PConnectionStateChange(state: String) {
            Log.i(TAG, "state  -----------------> : $state")
        }

        override fun onReceiveSignalingData(data: DataModel) {
            Log.i(TAG, "onReceiveSignalingData: ${data.type} --> ${data.data}")
        }

        override fun onReceiveChannelTextData(text: String?) {
            CoroutineScope(Dispatchers.Main).launch {
                receivedMessage.append("$text\n")
                binding.messageContainerTxt.text = receivedMessage.toString()
            }
        }

        override fun onReceiveChannelFileData(byteArray: ByteArray?) {
            // Not yet implemented
        }
    }
}