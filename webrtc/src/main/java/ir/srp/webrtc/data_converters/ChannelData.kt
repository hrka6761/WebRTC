package ir.srp.webrtc.data_converters

import org.webrtc.DataChannel

interface ChannelData {

    operator fun invoke(): Array<DataChannel.Buffer>
}