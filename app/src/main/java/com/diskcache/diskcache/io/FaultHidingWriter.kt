package com.diskcache.diskcache.io

import java.io.*
import java.nio.charset.StandardCharsets

class FaultHidingWriter(
    file: File,
    append: Boolean,
    val onError: (exception: Exception) -> Unit
): BufferedWriter(FileOutputStream(file, append).bufferedWriter(StandardCharsets.UTF_8)) {

    private var hasErrors = false

    override fun append(csq: CharSequence?): Writer {
        if (hasErrors) {
            return this
        }
        try {
            return super.append(csq)
        } catch (e: Exception) {
            hasErrors = true
            onError(e)
        }
        return this
    }

    override fun newLine() {
        try {
            super.newLine()
        } catch (e: Exception) {
            hasErrors = true
            onError(e)
        }
    }

    override fun flush() {
        try {
            super.flush()
        } catch (e: Exception) {
            hasErrors = true
            onError(e)
        }
    }

    override fun close() {
        try {
            super.close()
        } catch (e: IOException) {
            hasErrors = true
            onError(e)
        }
    }

}