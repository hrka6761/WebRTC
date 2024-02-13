package ir.srp.webrtc.data_converters

import ir.srp.webrtc.models.ChannelDataType
import org.webrtc.DataChannel
import java.io.File
import java.io.FileNotFoundException
import java.nio.ByteBuffer

class FileData(private val file: File) : ChannelData {

    override fun invoke(): Array<DataChannel.Buffer> {
        if (!file.exists())
            throw FileNotFoundException("The passed file is not exist.")

        val metaDataByteArray = ChannelDataType.File.name.toByteArray()
        val metaDataByteBuffer = ByteBuffer.wrap(metaDataByteArray)
        val metadata = DataChannel.Buffer(metaDataByteBuffer, false)

        val dataByteArray = file.readBytes()
        val dataByteBuffer = ByteBuffer.wrap(dataByteArray)
        val data = DataChannel.Buffer(dataByteBuffer, false)

        return arrayOf(metadata, data)
    }
}