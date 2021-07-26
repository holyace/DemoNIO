package com.michael.demo.nio.ext

fun Any?.hexHash(): String {
    this?: return "null"
    return "0x${Integer.toHexString(this.hashCode())}"
}