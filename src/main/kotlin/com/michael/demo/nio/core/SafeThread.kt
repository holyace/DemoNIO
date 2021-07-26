package com.michael.demo.nio.core

abstract class SafeThread: Thread() {

    final override fun run() {
        try {
            safeRun()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun safeRun()
}