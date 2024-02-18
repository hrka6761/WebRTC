package ir.srp.webrtc.exceptions

import okio.IOException

class NoSignallingConnectionException : IOException("Not yet connected to the signaling server.")