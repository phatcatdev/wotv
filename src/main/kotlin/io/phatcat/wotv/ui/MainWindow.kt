package io.phatcat.wotv.ui

import Strings.MATCHING_ELLIPSIS
import Strings.MATCHING_FAILED
import Strings.START_MATCHING
import Strings.SUCCESS
import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.phatcat.wotv.extensions.toImageBitmap
import io.phatcat.wotv.viewmodel.MainViewModel
import kotlinx.coroutines.flow.*
import java.io.File

private const val DEFAULT_SOURCE_FILE_PATH = "source.png"
private const val DEFAULT_TEMPLATE_FILE_PATH = "template.png"

@Suppress("FunctionName")
fun MainWindow() = Window {
  val windowScope = rememberCoroutineScope()
  var isMatching by remember { mutableStateOf(false) }
  var resultText by remember { mutableStateOf("") }
  var image = remember { File(DEFAULT_SOURCE_FILE_PATH).toImageBitmap() }

  val viewModel = MainViewModel()

  MaterialTheme {
    Column {
      Button(onClick = {
        if (isMatching) return@Button

        viewModel.performMatch(DEFAULT_SOURCE_FILE_PATH, DEFAULT_TEMPLATE_FILE_PATH)
          .onStart { isMatching = true }
          .onEach {
            resultText = SUCCESS
            image = it
          }
          .catch { resultText = MATCHING_FAILED }
          .onCompletion { isMatching = false }
          .launchIn(windowScope)

      }) {
        Text(if (isMatching) MATCHING_ELLIPSIS else START_MATCHING)
      }

      Text(
        text = resultText,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
      )

      Image(
        bitmap = image,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}
