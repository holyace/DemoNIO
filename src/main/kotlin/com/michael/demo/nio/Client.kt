package com.michael.demo.nio

import com.michael.demo.nio.core.BaseSocketChannel2
import com.michael.demo.nio.ext.hexHash
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class Client(private val host: String, private val port: Int): BaseSocketChannel2() {

    override fun safeRun() {
        val channel = SocketChannel.open()
        channel.configureBlocking(false)

        val selector = Selector.open()

        channel.register(selector, SelectionKey.OP_CONNECT or SelectionKey.OP_READ)

        channel.connect(InetSocketAddress(host, port))

        println("[Client@${channel.hexHash()}]: waiting for server accept...")

        var connected = false

        var timestamp = System.currentTimeMillis()
        var count = 0

        while (true) {
            val readyCount = selector.select()

            if (readyCount > 0) {
                val selectedKeys = selector.selectedKeys()
                val iterator = selectedKeys.iterator()

                while (iterator.hasNext()) {
                    val selectionKey = iterator.next()

                    when {
                        selectionKey.isConnectable -> {
                            onConnectToServer(channel)
                            connected = true
                        }

                        selectionKey.isReadable -> {
                            val socketChannel = selectionKey.channel() as SocketChannel
                            onReceiveFromServer(socketChannel)
                        }
                    }
                }
                selectedKeys.clear()
            }

            val now = System.currentTimeMillis()
            if (connected && now - timestamp >= 3000) {
                timestamp = now
                val ret = sendData(channel, "client heart bit $count")
                if (ret.ret != 0) {
                    println("client heart bit $count fail: $ret")
                }
                count++
            }
        }
    }

    private fun onConnectToServer(clientChannel: SocketChannel) {
        val ret = clientChannel.finishConnect()
        val msg = "server, you can speak now"
        println("[Client@${clientChannel.hexHash()}]: connect to server $ret, send msg: $msg to server")
        sendData(clientChannel, msg)
    }

    private fun onReceiveFromServer(clientChannel: SocketChannel) {
        val msg = readData(clientChannel)
        if (msg.isNullOrEmpty()) {
            println("[Client@${clientChannel.hexHash()}]: receive server empty msg")
            return
        }

        println("[Client@${clientChannel.hexHash()}]: receive server msg = $msg")
//
//        sendData(clientChannel, "server, i receive you msg")
    }
}