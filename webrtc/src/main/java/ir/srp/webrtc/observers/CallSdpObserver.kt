package ir.srp.webrtc.observers

import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.models.DataType
import ir.srp.webrtc.webSocket.WebSocketClient
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

class CallSdpObserver(
    private val peerConnection: PeerConnection,
    private val signalingServerConnection: WebSocketClient,
    private val origin: String,
    private val destination: String,
) : SdpObserver {

    private var _sd: SessionDescription? = null


    override fun onCreateSuccess(sd: SessionDescription?) {
        _sd = sd
        peerConnection.setLocalDescription(this, _sd)
    }

    override fun onSetSuccess() {
        signalingServerConnection.sendData(
            DataModel(
                type = DataType.Call,
                username = origin,
                target = destination,
                data = _sd?.description
            )
        )
    }

    override fun onCreateFailure(p0: String?) {
        // Not yet implemented
    }

    override fun onSetFailure(p0: String?) {
        // Not yet implemented
    }
}