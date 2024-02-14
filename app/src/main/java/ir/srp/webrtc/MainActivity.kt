package ir.srp.webrtc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import ir.srp.webrtc.data_converters.TextData
import ir.srp.webrtc.databinding.ActivityMainBinding
import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.models.IceServerModel
import ir.srp.webrtc.utils.IceServerBuilder.createListOfIceServers
import ir.srp.webrtc.webSocket.WebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "HAMIDREZA"
    private val SIGNALING_SERVER_URL = "ws://192.168.54.187:3000"
    private lateinit var binding: ActivityMainBinding
    private lateinit var p2PChannel: P2PChannel
    private val receivedMessage = StringBuffer("")
    private var signalingServerConnection: WebSocketClient? = null
    private var isP2PConnectionCreated = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize()
    }

    private fun initialize() {
        initSignalingServerConnectionButton()
        initSignInButton()
        initHandShakeButton()
        initSendMessageButton()
    }

    private fun initSignalingServerConnectionButton() {
        binding.signalingServerConnectionBtn.setOnClickListener {
            if (signalingServerConnection != null)
                displayDialog(
                    "Signaling server connection",
                    "Are you sure you want to disconnect from the signaling server ?",
                    { signalingServerConnection?.removeConnection() },
                    {}
                )
        }
    }

    private fun initSignInButton() {
        binding.signInBtn.setOnClickListener {
            val username = binding.usernameEdt.text.toString()
            if (username.isNotEmpty()) {
                binding.usernameEdtl.isEnabled = false
                binding.signInBtn.isEnabled = false
                p2PChannel = createP2PChannelBuilder(username).build()
            } else
                warning("Please enter a username")
        }
    }

    private fun initHandShakeButton() {
        binding.handshakeBtn.setOnClickListener {
            val target = binding.targetUsernameEdt.text.toString()
            if (target.isNotEmpty())
                p2PChannel.handshake(target)
            else
                warning("Please enter a target")
        }
    }

    private fun initSendMessageButton() {
        binding.sendMessageBtn.setOnClickListener {
            val message = binding.messageEdt.text.toString()
            if (message.isNotEmpty() && isP2PConnectionCreated)
                p2PChannel.sendData(TextData(message))
            else
                warning("Please enter a message")
        }
    }

    private fun createP2PChannelBuilder(username: String): P2PChannel.Companion.Builder {
        return P2PChannel.Companion.Builder(
            application,
            SIGNALING_SERVER_URL,
            username,
            createListOfIceServers(
                IceServerModel(
                    "turn:openrelay.metered.ca:443?transport=tcp",
                    "openrelayproject",
                    "openrelayproject"
                )
            )
        ).setEventsListener(ChannelEventListener())
    }

    private fun warning(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(binding.root, message, duration).show()
    }

    private fun displayDialog(
        title: String,
        message: String,
        positiveCallback: () -> Unit,
        negativeCallback: () -> Unit,
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("ok") { dialog, _ ->
                positiveCallback()
                dialog.dismiss()
            }
            .setNegativeButton("cancel") { dialog, _ ->
                negativeCallback()
                dialog.dismiss()
            }
            .show()
    }

    private fun setButtonBackgroundColor(btn: MaterialButton, colorId: Int) {
        btn.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, colorId)
    }


    private inner class ChannelEventListener : ChannelEventsListener {
        override fun onSuccessSignalingServerConnection(webSocket: WebSocketClient) {
            CoroutineScope(Dispatchers.Main).launch {
                signalingServerConnection = webSocket
                setButtonBackgroundColor(binding.signalingServerConnectionBtn, R.color.green)
                binding.targetUsernameEdtl.isEnabled = true
                binding.handshakeBtn.isEnabled = true
            }
        }

        override fun onFailedSignalingServerConnection(t: Throwable) {
            CoroutineScope(Dispatchers.Main).launch {
                setButtonBackgroundColor(binding.signalingServerConnectionBtn, R.color.red)
                if (!isP2PConnectionCreated) {
                    binding.usernameEdtl.isEnabled = true
                    binding.signInBtn.isEnabled = true
                }
                binding.targetUsernameEdtl.isEnabled = false
                binding.handshakeBtn.isEnabled = false
                warning(t.message.toString())
                signalingServerConnection = null
            }
        }

        override fun onCLoseSignalingServerConnection(code: Int, reason: String) {
            CoroutineScope(Dispatchers.Main).launch {
                setButtonBackgroundColor(binding.signalingServerConnectionBtn, R.color.red)
                if (!isP2PConnectionCreated) {
                    binding.usernameEdtl.isEnabled = true
                    binding.signInBtn.isEnabled = true
                }
                binding.targetUsernameEdtl.isEnabled = false
                binding.handshakeBtn.isEnabled = false

                if (reason.isNotEmpty())
                    warning(reason)
                else
                    warning("You disconnect from signaling server.")

                signalingServerConnection = null
            }
        }

        override fun onCreateP2PConnection() {
            CoroutineScope(Dispatchers.Main).launch {
                isP2PConnectionCreated = true
                setButtonBackgroundColor(binding.webrtcConnectionBtn, R.color.green)
                binding.handshakeBtn.isEnabled = false
                binding.targetUsernameEdtl.isEnabled = false
                binding.messageEdt.isEnabled = true
                binding.sendMessageBtn.isEnabled = true
            }
        }

        override fun onDestroyP2PConnection() {
            CoroutineScope(Dispatchers.Main).launch {
                isP2PConnectionCreated = false
                setButtonBackgroundColor(binding.webrtcConnectionBtn, R.color.red)
                if (signalingServerConnection == null) {
                    binding.usernameEdtl.isEnabled = true
                    binding.signInBtn.isEnabled = true
                } else {
                    binding.targetUsernameEdtl.isEnabled = true
                    binding.handshakeBtn.isEnabled = true
                }
                binding.messageEdt.isEnabled = false
                binding.sendMessageBtn.isEnabled = false
            }
        }

        override fun onP2PConnectionStateChange(state: String) {
            Log.i(TAG, "state  -----------------> : $state")
        }

        override fun onReceiveSignalingData(data: DataModel) {
            Log.i(TAG, "onReceiveSignalingData: ${data.type} --> ${data.data}")
        }

        override fun onReceiveChannelTextData(text: String?) {
            CoroutineScope(Dispatchers.Main).launch {
                receivedMessage.append("$text\n")
                binding.messageContainerTxt.text = receivedMessage.toString()
            }
        }

        override fun onReceiveChannelFileData(byteArray: ByteArray?) {
            // Not yet implemented
        }
    }
}