package com.diskcache.diskcache.io

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.Writer
import java.nio.charset.StandardCharsets

class FakeWriter(file: File): BufferedWriter(FileOutputStream(file, true).bufferedWriter(StandardCharsets.UTF_8)) {

    override fun append(csq: CharSequence?): Writer {
        return this
    }

    override fun newLine() {

    }

}