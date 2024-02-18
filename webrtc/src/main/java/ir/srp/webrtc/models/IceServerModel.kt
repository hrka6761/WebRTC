package ir.srp.webrtc.models

data class IceServerModel(
    val uri: String,
    val username: String? = null,
    val password: String? = null,
)