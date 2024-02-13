package ir.srp.webrtc.webSocket

import ir.srp.webrtc.utils.Constants.CLOSE_WEBSOCKET_STATUS_CODE
import ir.srp.webrtc.utils.Constants.CLOSE_WEBSOCKET_STATUS_REASON
import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.data_converters.JsonConverter.convertObjectToJsonString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient(
    private val url: String,
    private val listener: WebSocketListener,
) {

    private val okHttpClient = OkHttpClient()
    private lateinit var request: Request
    private lateinit var webSocket: WebSocket
    private var isConnected = false


    fun createConnection() {
        request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, listener)
        isConnected = true
    }

    fun removeConnection() {
        webSocket.close(CLOSE_WEBSOCKET_STATUS_CODE, CLOSE_WEBSOCKET_STATUS_REASON)
        isConnected = false
    }

    fun sendData(data: DataModel) {
        if (isConnected)
            webSocket.send(convertObjectToJsonString(data))
    }
}