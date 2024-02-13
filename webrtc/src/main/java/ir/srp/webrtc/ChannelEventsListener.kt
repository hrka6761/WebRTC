package ir.srp.webrtc

import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.webSocket.WebSocketClient
import java.io.File

interface ChannelEventsListener {

    fun onSuccessSignalingServerConnection(webSocket: WebSocketClient)
    fun onFailedSignalingServerConnection(t: Throwable)
    fun onCLoseSignalingServerConnection(code: Int, reason: String)
    fun onCreateP2PChannel()
    fun onReceiveSignalingData(data: DataModel)
    fun onReceiveChannelTextData(text: String?)
    fun onReceiveChannelFileData(byteArray: ByteArray?)
}