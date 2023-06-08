package com.diskcache.diskcache.io

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

fun File.appendOutputStream() = FileOutputStream(this, true).writer(StandardCharsets.UTF_8)