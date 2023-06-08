package com.diskcache.diskcache.io

import java.io.BufferedWriter
import java.io.File
import java.io.Writer

class FakeWriter(file: File): BufferedWriter(file.appendOutputStream()) {

    override fun append(csq: CharSequence?): Writer {
        return this
    }

    override fun newLine() {

    }

}