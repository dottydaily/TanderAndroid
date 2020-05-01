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
//    private const val url = "https://socketio-dot-tander-webservice.an.r.appspot.com"
    val socket: Socket // = IO.socket("http://127.0.0.1:65080")
    var liveUpdate = MutableLiveData<Boolean>().apply { value = false }

    init {
        val opts = IO.Options()
//        opts.port = 65080
//        opts.forceNew = true
////        opts.reconnection = true
//        opts.hostname = "ws://192.168.1.102"
//        opts.host = "192.168.1.102"
//        opts.transports = arrayOf(WebSocket.NAME)
//        opts.secure = true
//        socket = IO.socket(url, opts)
        socket = IO.socket(url)
//
//        println(opts.hostname)

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
    }

    fun start() {
        if (!socket.connected()) {
            socket.connect()
            println("[Socket.IO] Initialize service...")
        } else {
            println("[Socket.IO] Already connected")
        }
    }

    // start observe status
    fun startObserver(owner: LifecycleOwner,
                      update: Observer<Boolean>) {
        println("Now observe with status = ${socket.connected()}")
        liveUpdate.observe(owner, update)
    }

    // remove observer, and remove all status
    fun clearObserver(owner: LifecycleOwner) {
        liveUpdate.apply {
            removeObservers(owner)
            value = false
        }

        socket.off("update all lobbies")
    }
}