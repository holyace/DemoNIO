package com.michael.demo.nio.core

import com.michael.demo.nio.core.transfer.ChannelReader
import com.michael.demo.nio.core.transfer.ChannelWriter
import com.michael.demo.nio.core.transfer.SocketDTO
import java.nio.channels.SocketChannel

abstract class BaseSocketChannel2: SafeThread() {

    private val mWriter by lazy { ChannelWriter() }
    private val mReader by lazy { ChannelReader() }

    fun sendData(socketChannel: SocketChannel, data: String?): Result {
        if (data.isNullOrEmpty()) return Result(-1)

        val dto = SocketDTO.of(data)
        val ret = mWriter.write(socketChannel, dto)
        val retCode = if (ret) 0 else -2
        return Result(retCode)
    }

    fun readData(socketChannel: SocketChannel): String? {
        val dto = mReader.read(socketChannel)
        dto?: return null
        if (dto.getType() != SocketDTO.TYPE_STRING) return null
        if (dto.getSize() <= 0) return null
        dto.getData()?: return null
        return String(dto.getData()!!, Charsets.UTF_8)
    }
}