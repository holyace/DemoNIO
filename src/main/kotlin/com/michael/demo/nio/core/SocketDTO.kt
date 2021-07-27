package com.michael.demo.nio.core

import com.michael.demo.nio.ext.isEmpty
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import kotlin.math.min

class SocketDTO private constructor() {

    companion object {
        const val BUFFER_SIZE = 10

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

        fun read(socketChannel: SocketChannel, readTo: SocketDTO? = null, offset: Int = 0): SocketDTO? {
            var totalRead = offset
            var ret = readTo
            if (!mReadBuffer.isEmpty()) {
                ret = readTo?: SocketDTO()
                totalRead += readBuffer(mReadBuffer, ret, offset)
            }

            mReadBuffer.clear()
            val read = socketChannel.read(mReadBuffer)
            if (read <= 0) return readTo

            if (ret == null) ret = SocketDTO()
            mReadBuffer.flip()
            totalRead += readBuffer(mReadBuffer, ret, totalRead)

            if (ret.mSize - totalRead > 0) {
                read(socketChannel, ret, totalRead)
            }
            return ret
        }

        private fun readBuffer(byteBuffer: ByteBuffer, dest: SocketDTO, offset: Int = 0): Int {
            if (dest.mSize == 0 && byteBuffer.hasRemaining()) {
                dest.mSize = byteBuffer.get().toInt()
                dest.mData = ByteArray(dest.mSize)
            }
            if (dest.mType == TYPE_UNKNOWN && byteBuffer.hasRemaining()) {
                dest.mType = byteBuffer.get().toInt()
            }
            val leftToRead = dest.mSize - offset
            val realRead = min(byteBuffer.remaining(), leftToRead)
            if (realRead > 0) {
                byteBuffer.get(dest.mData, offset, realRead)
            }
            return realRead
        }
    }

    class Writer {

        private val mWriteBuffer = ByteBuffer.allocate(BUFFER_SIZE)

        fun write(socketChannel: SocketChannel, socketDTO: SocketDTO, offset: Int = 0, length: Int = socketDTO.mSize): Boolean {
            mWriteBuffer.clear()
            if (offset <= 0) {
                mWriteBuffer.put(socketDTO.mSize.toByte())
                mWriteBuffer.put(socketDTO.mType.toByte())
            }
            val remaining = mWriteBuffer.remaining()
            val write = min(remaining, length)
            mWriteBuffer.put(socketDTO.mData, offset, write)
            val position = offset + write
            mWriteBuffer.flip()
            socketChannel.write(mWriteBuffer)
            if (socketDTO.mSize > position) {
                return write(socketChannel, socketDTO, position, socketDTO.mSize - position)
            }
            return true
        }

        private fun writeInternal(socketChannel: SocketChannel, data: ByteBuffer) {
            socketChannel.write(data)
        }
    }
}