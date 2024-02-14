package ir.srp.webrtc

import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.webSocket.WebSocketClient

interface ChannelEventsListener {

    fun onSuccessSignalingServerConnection(webSocket: WebSocketClient)
    fun onFailedSignalingServerConnection(t: Throwable)
    fun onCLoseSignalingServerConnection(code: Int, reason: String)
    fun onCreateP2PConnection()
    fun onDestroyP2PConnection()
    fun onP2PConnectionStateChange(state: String)
    fun onReceiveSignalingData(data: DataModel)
    fun onReceiveChannelTextData(text: String?)
    fun onReceiveChannelFileData(byteArray: ByteArray?)
}