package ir.srp.webrtc.webSocket

import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.data_converters.JsonConverter.convertJsonStringToObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketListener(
    private val onOpenCallback: (response: Response) -> Unit,
    private val onClosedCallback: (code: Int, reason: String) -> Unit,
    private val onClosingCallback: (code: Int, reason: String) -> Unit,
    private val onFailureCallback: (throwable: Throwable, response: Response?) -> Unit,
    private val onMessageCallback: (message: DataModel) -> Unit,
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)

        CoroutineScope(Dispatchers.Main).launch {
            onOpenCallback(response)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)

        CoroutineScope(Dispatchers.Main).launch {
            onClosedCallback(code, reason)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)

        CoroutineScope(Dispatchers.Main).launch {
            onClosingCallback(code, reason)
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)

        CoroutineScope(Dispatchers.Main).launch {
            onFailureCallback(t, response)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)

        CoroutineScope(Dispatchers.Main).launch {
            onMessageCallback(
                convertJsonStringToObject(
                    text,
                    DataModel::class.java
                ) as DataModel
            )
        }
    }
}