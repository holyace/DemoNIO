package com.michael.demo.nio.ext

import java.nio.ByteBuffer

fun ByteBuffer.readLine(): ByteArray? {
    val end = '\n'.toByte()
    if (!hasRemaining()) return null
    val bytes = mutableListOf<Byte>()
    var byte: Byte? = null
    while (this.hasRemaining() && get().apply { byte = this } != end) {
        bytes.add(byte!!)
    }
    return bytes.toByteArray()
}
