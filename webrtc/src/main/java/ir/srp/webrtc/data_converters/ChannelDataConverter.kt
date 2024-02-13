package ir.srp.webrtc.data_converters

import org.webrtc.DataChannel

object ChannelDataConverter {

    fun convertChannelDataToMetaData(buffer: DataChannel.Buffer): String =
        convertChannelDataToText(buffer)

    fun convertChannelDataToText(buffer: DataChannel.Buffer): String {
        val dataByteBuffer = buffer.data
        val dataByteBufferSize = dataByteBuffer.remaining()
        val data = ByteArray(dataByteBufferSize)

        buffer.data[data]

        return String(data, Charsets.UTF_8)
    }

    fun convertChannelDataToFile(buffer: DataChannel.Buffer): ByteArray {
        val dataByteBuffer = buffer.data
        val dataByteBufferSize = dataByteBuffer.remaining()
        val data = ByteArray(dataByteBufferSize)

        buffer.data[data]

        return data
    }
}