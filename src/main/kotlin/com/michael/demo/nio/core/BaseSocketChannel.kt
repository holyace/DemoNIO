package com.michael.demo.nio.core

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

abstract class BaseSocketChannel: SafeThread() {

    companion object {
        const val RET_EMPTY_MSG = 1
        const val RET_MSG_TOO_LARGE = 2

        fun id(any: Any?): String {
            any?: return "null"
            return "0x${Integer.toHexString(any.hashCode())}"
        }
    }

    private val mBufferSize = 1024
    private val mMaxBufferSize = 1024 * 8
    private val mDefaultCharset = Charsets.UTF_8

    private val mReadBuffer by lazy { ByteBuffer.allocate(mBufferSize) }
    private val mWriteBuffer by lazy { ByteBuffer.allocate(mBufferSize) }

    fun sendData(channel: SocketChannel, data: String): Result {
        if (data.isEmpty()) return Result(RET_EMPTY_MSG)
        val dataBytes = "$data\n".toByteArray(mDefaultCharset)
        val dataSize = dataBytes.size

        if (dataSize > mBufferSize) {
            return Result(RET_MSG_TOO_LARGE)
        }

        mWriteBuffer.clear()
        mWriteBuffer.put(dataBytes)
        val write = sendData(channel, mWriteBuffer)

        return Result(0, write)
    }

    private fun sendData(channel: SocketChannel, data: ByteBuffer): Int {
        data.flip()
        var write = 0
        while (data.hasRemaining()) {
            write += channel.write(data)
        }
        return write
    }

    fun readData(channel: SocketChannel): String? {
        val bytes = ByteArray(mMaxBufferSize)
        val read = readData(channel, mReadBuffer)
        return if (read > 0) {
            mReadBuffer.flip()
            mReadBuffer.get(bytes, 0, read)
            String(bytes, mDefaultCharset)
        } else {
            null
        }
    }

    private fun readData(channel: SocketChannel, data: ByteBuffer): Int {
        data.clear()
        return channel.read(data)
    }

}