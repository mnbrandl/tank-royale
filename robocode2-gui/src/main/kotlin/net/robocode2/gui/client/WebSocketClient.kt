package net.robocode2.gui.client

import com.beust.klaxon.Klaxon
import net.robocode2.gui.utils.Event
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketClient(private val uri: URI): AutoCloseable {

    val onOpen = Event<Unit>()
    val onClose = Event<Unit>()
    val onMessage = Event<String>()
    val onError = Event<Exception>()

    private var client = Client()

    fun open() {
        client.connect()
    }

    override fun close() {
        client.close()
    }

    fun isOpen() = client.isOpen

    fun send(content: Any) {
        client.send(Klaxon().toJsonString(content))
    }

    private inner class Client : WebSocketClient(uri) {

        override fun onOpen(serverHandshake: ServerHandshake?) {
            onOpen.publish(Unit)
            println("onOpen")
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            onClose.publish(Unit)
            println("onClose: (code: $code, reason: $reason, remote: $remote)")
        }

        override fun onMessage(message: String) {
            onMessage.publish(message)
            println("onMessage: $message")
        }

        override fun onError(ex: Exception) {
            onError.publish(ex)
            println("onError: $ex")
        }
    }
}