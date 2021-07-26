package com.michael.demo.nio.core

import java.nio.channels.SocketChannel

abstract class BaseSocketChannel2: SafeThread() {

    fun sendData(socketChannel: SocketChannel, data: String?): Result {
        if (data.isNullOrEmpty()) return Result(-1)

        val dto = SocketDTO.of(data)
        val ret = SocketDTO.Writer().write(socketChannel, dto)
        val retCode = if (ret) 0 else -2
        return Result(retCode)
    }

    fun readData(socketChannel: SocketChannel): String? {
        val dto = SocketDTO.Reader().read(socketChannel)
        dto?: return null
        dto.getData()?: return null
        return String(dto.getData()!!, Charsets.UTF_8)
    }
}