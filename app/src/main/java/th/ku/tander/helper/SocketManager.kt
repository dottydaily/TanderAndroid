package th.ku.tander.helper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.SocketIOException
import io.socket.engineio.client.EngineIOException
import io.socket.engineio.client.transports.WebSocket

object SocketManager {
//    private const val url = "http://192.168.1.102:65080"
    private const val url = "https://tander-socketio.herokuapp.com"
//    private const val url = "http://socketio-dot-tander-webservice.an.r.appspot.com:65080"
    val socket: Socket // = IO.socket("http://127.0.0.1:65080")
    var hasUpdate = MutableLiveData<String>().apply { value = "WAIT" }

    init {
        socket = IO.socket(url)

        socket.on(Socket.EVENT_CONNECT) {
            println("[Socket.IO] Connect at $url")
        }.on(Socket.EVENT_RECONNECTING) {
            println("[Socket.IO] Reconnecting...")
        }.on(Socket.EVENT_CONNECT_ERROR) {
            println("[Socket.IO] Connection Error")
//            val exception = it[0] as EngineIOException
//            exception.run {
//                printStackTrace()
//            }
        }.on(Socket.EVENT_DISCONNECT) {
            println("[Socket.IO] Disconnected")
        }.on(Socket.EVENT_PING) {
            println("[Socket.IO] Ping")
        }.on(Socket.EVENT_PONG) {
            println("[Socket.IO] Pong")
        }
        socket.on("update all lobbies") {
            hasUpdate.run {
                println("[Socket.IO] $it -> $value")
                postValue("UPDATE")
            }
        }
    }

    fun start() {
        if (!socket.connected()) {
            socket.connect()
            println("[Socket.IO] Initialize service...")
        } else {
            println("[Socket.IO] Already connected")
        }
    }
}