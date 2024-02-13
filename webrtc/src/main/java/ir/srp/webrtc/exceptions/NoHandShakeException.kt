package ir.srp.webrtc.exceptions

import okio.IOException

class NoHandShakeException : IOException("The handshake is not done")