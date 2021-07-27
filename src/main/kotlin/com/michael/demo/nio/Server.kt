package com.michael.demo.nio

import com.michael.demo.nio.core.BaseSocketChannel2
import com.michael.demo.nio.ext.hexHash
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class Server(private val port: Int): BaseSocketChannel2() {

    override fun safeRun() {
        val serverChannel = ServerSocketChannel.open()
        serverChannel.configureBlocking(false)
        serverChannel.socket().bind(InetSocketAddress(port))
        val selector = Selector.open()
        serverChannel.register(selector, SelectionKey.OP_ACCEPT)

        println("[Server@${serverChannel.hexHash()}]: wait for client...")

        val clientList = mutableListOf<SocketChannel>()
        var timestamp = System.currentTimeMillis()
        var count = 0

        while (true) {
            val readyCount = selector.select()
            if (readyCount > 0) {
                val selectedKeys = selector.selectedKeys()
                val iterator = selectedKeys.iterator()

                loop@ while (iterator.hasNext()) {
                    val selectionKey = iterator.next()

                    if (selectionKey.isAcceptable) {
                        val clientChannel: SocketChannel? = serverChannel.accept()
                        if (clientChannel == null) {
                            println("accept null client")
                            continue@loop
                        }
                        clientChannel.configureBlocking(false)
                        clientChannel.register(selector, SelectionKey.OP_CONNECT or SelectionKey.OP_READ)
                        clientList.add(clientChannel)
                        onAcceptClient(serverChannel, clientChannel)
                    } else if (selectionKey.isReadable) {
                        val channel = selectionKey.channel() as SocketChannel
                        onReceiveClientMessage(serverChannel, channel)
                    }
                }
                selectedKeys.clear()
            }

            val now = System.currentTimeMillis()
            if (now - timestamp >= 3000) {
                timestamp = now
                clientList.forEach {
                    val ret = sendData(it, "server heart bit $count")
                    if (ret.ret != 0) {
                        println("server heart bit $count fail: $ret")
                    }
                }
                count++
            }
        }
    }

    private fun onAcceptClient(serverChannel: ServerSocketChannel, clientChannel: SocketChannel) {

        println("[Server@${serverChannel.hexHash()}]: accept client ${serverChannel.hexHash()}")

        val msg = "[Server@${serverChannel.hexHash()}]: client, you can speak now"

        val ret = sendData(clientChannel, msg)

        println("[Server@${serverChannel.hexHash()}]: send msg to client ${serverChannel.hexHash()} ret: $ret, msg: $msg")
    }

    private fun onReceiveClientMessage(serverChannel: ServerSocketChannel, clientChannel: SocketChannel) {
        val msg = readData(clientChannel)
        if (msg.isNullOrEmpty()) {
            println("[Server@${serverChannel.hexHash()}]: read Client[${serverChannel.hexHash()}] empty msg")
            return
        }
        println("[Server@${serverChannel.hexHash()}]: receive [Client@${serverChannel.hexHash()}] msg = $msg")
    }
}