package com.michael.demo.nio.core

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import kotlin.math.min

class SocketDTO {

    companion object {
        const val BUFFER_SIZE = 1024

        const val TYPE_UNKNOWN = 0
        const val TYPE_STRING = 1
        const val TYPE_BINARY = 1
        const val TYPE_JSON = 1

        fun of(data: String, type: Int = TYPE_STRING, charset: Charset = Charsets.UTF_8): SocketDTO {
            return SocketDTO().apply {
                mType = type
                mData = data.toByteArray(charset)
                mSize = mData!!.size
            }
        }

        fun of(data: ByteArray): SocketDTO {
            return SocketDTO().apply {
                mType = TYPE_BINARY
                mData = data
                mSize = data.size
            }
        }
    }

    private var mSize = 0
    private var mType = TYPE_UNKNOWN
    private var mData: ByteArray? = null

    fun getSize(): Int = mSize

    fun getType(): Int = mType

    fun getData(): ByteArray? = mData

    class Reader {

        private val mReadBuffer = ByteBuffer.allocate(BUFFER_SIZE)

        fun read(socketChannel: SocketChannel, size: Int = BUFFER_SIZE, readTo: SocketDTO? = null): SocketDTO? {
            mReadBuffer.clear()
            var read = socketChannel.read(mReadBuffer)
            if (read <= 0) return readTo

            mReadBuffer.flip()

            val ret = readTo ?: SocketDTO()

            ret.mSize = mReadBuffer.get().toInt()
            ret.mType = mReadBuffer.get().toInt()
            val body = ByteArray(size)
            val remaining = mReadBuffer.remaining()
            mReadBuffer.get(body, 0, min(remaining, size))
            ret.mData = body
            if (size > remaining) {
                return read(socketChannel, size - read, ret)
            }

            return ret
        }
    }

    class Writer {

        private val mWriteBuffer = ByteBuffer.allocate(BUFFER_SIZE)

        fun write(socketChannel: SocketChannel, socketDTO: SocketDTO, offset: Int = 0, length: Int = socketDTO.mSize): Boolean {
            mWriteBuffer.clear()
            mWriteBuffer.put(socketDTO.mSize.toByte())
            mWriteBuffer.put(socketDTO.mType.toByte())
            val remaining = mWriteBuffer.remaining()
            val write = min(remaining, length)
            mWriteBuffer.put(socketDTO.mData, offset, write)
            mWriteBuffer.flip()
            socketChannel.write(mWriteBuffer)
            if (socketDTO.mSize > remaining) {
                return write(socketChannel, socketDTO, write, socketDTO.mSize - write)
            }
            return true
        }

        private fun writeInternal(socketChannel: SocketChannel, data: ByteBuffer) {
            socketChannel.write(data)
        }
    }
}