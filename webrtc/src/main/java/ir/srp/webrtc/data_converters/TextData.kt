package ir.srp.webrtc.data_converters

import ir.srp.webrtc.models.ChannelDataType
import org.webrtc.DataChannel
import java.nio.ByteBuffer

class TextData(private val text: String) : ChannelData {

    override fun invoke(): Array<DataChannel.Buffer> {
        val metaDataByteArray = ChannelDataType.Text.name.toByteArray()
        val metaDataByteBuffer = ByteBuffer.wrap(metaDataByteArray)
        val metadata = DataChannel.Buffer(metaDataByteBuffer, false)

        val dataByteArray = text.toByteArray()
        val dataByteBuffer = ByteBuffer.wrap(dataByteArray)
        val data = DataChannel.Buffer(dataByteBuffer, false)

        return arrayOf(metadata, data)
    }
}