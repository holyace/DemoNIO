package com.michael.demo.nio

fun main(args: Array<String>) {

    val port = 8899

    val server = Server(port)
    server.start()

    val client = Client("localhost", port)
    client.start()
}



