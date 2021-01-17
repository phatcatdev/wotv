package io.phatcat.wotv.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

fun File.toImageBitmap(): ImageBitmap = org.jetbrains.skija.Image.makeFromEncoded(readBytes()).asImageBitmap()
