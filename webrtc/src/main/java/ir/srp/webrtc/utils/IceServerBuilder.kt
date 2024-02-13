package ir.srp.webrtc.utils

import ir.srp.webrtc.models.IceServerModel
import org.webrtc.PeerConnection.IceServer

object IceServerBuilder {

    fun createListOfIceServers(vararg iceServerModels: IceServerModel): List<IceServer> {
        val iceServers = mutableListOf<IceServer>()

        for (iceServerModel in iceServerModels)
            iceServers.add(
                IceServer.builder(iceServerModel.uri)
                    .setUsername(iceServerModel.username)
                    .setPassword(iceServerModel.password)
                    .createIceServer()
            )

        return iceServers
    }
}