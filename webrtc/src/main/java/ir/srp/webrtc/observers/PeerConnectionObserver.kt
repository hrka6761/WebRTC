package ir.srp.webrtc.observers

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver

class PeerConnectionObserver(
    private val onProvideDataChannel: (dataChannel: DataChannel?) -> Unit,
    private val onProvideIceCandidate: (iceCandidate: IceCandidate?) -> Unit
) : PeerConnection.Observer {

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        // Not yet implemented
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        // Not yet implemented
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        // Not yet implemented
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        // Not yet implemented
    }

    override fun onIceCandidate(iceCandidate: IceCandidate?) {
        onProvideIceCandidate(iceCandidate)
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        // Not yet implemented
    }

    override fun onAddStream(p0: MediaStream?) {
        // Not yet implemented
    }

    override fun onRemoveStream(p0: MediaStream?) {
        // Not yet implemented
    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        onProvideDataChannel(dataChannel)
    }

    override fun onRenegotiationNeeded() {
        // Not yet implemented
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        // Not yet implemented
    }
}