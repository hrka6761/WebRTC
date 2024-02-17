package ir.srp.webrtc.observers

import ir.srp.webrtc.ChannelEventsListener
import ir.srp.webrtc.data_converters.ChannelDataConverter.convertChannelDataToMetaData
import ir.srp.webrtc.data_converters.ChannelDataConverter.convertChannelDataToFile
import ir.srp.webrtc.data_converters.ChannelDataConverter.convertChannelDataToText
import ir.srp.webrtc.models.ChannelDataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.DataChannel

class DataChannelObserver(
    private val eventsListener: ChannelEventsListener?,
) : DataChannel.Observer {

    private var receivedDataType: String? = null


    override fun onBufferedAmountChange(p0: Long) {
        // Not yet implemented
    }

    override fun onStateChange() {
        // Not yet implemented
    }

    override fun onMessage(buffer: DataChannel.Buffer?) {
        CoroutineScope(Dispatchers.Main).launch {
            if (receivedDataType == null)
                receivedDataType = buffer?.let { convertChannelDataToMetaData(it) }
            else {
                if (receivedDataType == ChannelDataType.Text.name) {
                    val data = buffer?.let { convertChannelDataToText(it) }
                    eventsListener?.onReceiveChannelTextData(data)
                } else {
                    val data = buffer?.let { convertChannelDataToFile(it) }
                    eventsListener?.onReceiveChannelFileData(data)
                }
                receivedDataType = null
            }
        }
    }
}