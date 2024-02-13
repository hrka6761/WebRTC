package ir.srp.webrtc.exceptions

class IllegalUrlException :
    IllegalArgumentException("Signaling server url is not correct. it must be like ws://server_address:port or wss://server_address:port")