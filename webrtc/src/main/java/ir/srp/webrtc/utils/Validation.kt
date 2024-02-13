package ir.srp.webrtc.utils

import java.util.regex.Pattern

object Validation {

    fun isValidSignalingServerUrl(url: String): Boolean {
        val urlPattern = "^(ws|wss)://[a-zA-Z0-9.-]+:[0-9]+\$"
        val pattern: Pattern = Pattern.compile(urlPattern)
        val matcher = pattern.matcher(url)

        return matcher.matches()
    }
}