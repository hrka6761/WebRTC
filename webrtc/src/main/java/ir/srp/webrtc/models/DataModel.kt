package ir.srp.webrtc.models

data class DataModel(
    var type: DataType,
    var username: String,
    var target: String? = null,
    var data: Any? = null
)