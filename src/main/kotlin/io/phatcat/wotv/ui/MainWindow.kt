package io.phatcat.wotv.ui

import Strings.MATCHING_ELLIPSIS
import Strings.MATCHING_FAILED
import Strings.START_MATCHING
import Strings.SUCCESS
import Strings.TITLE
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.phatcat.wotv.extensions.toImageBitmap
import io.phatcat.wotv.viewmodel.MainViewModel
import io.phatcat.wotv.viewmodel.MainViewModel.*
import kotlinx.coroutines.flow.*
import java.io.File

private const val DEFAULT_SOURCE_FILE_PATH = "source.png"
private const val DEFAULT_TEMPLATE_FILE_PATH = "template.png"

@Suppress("FunctionName")
fun MainWindow() = Window(title = TITLE) {
  val windowScope = rememberCoroutineScope()
  var isMatching by remember { mutableStateOf(false) }
  var resultText by remember { mutableStateOf("") }
  var image = remember { File(DEFAULT_SOURCE_FILE_PATH).toImageBitmap() }
  val stateVertical = rememberScrollState(0f)
  val viewModel = MainViewModel()

  MaterialTheme {
    Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
      ScrollableColumn(
        modifier = Modifier.fillMaxSize().padding(end = 10.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        scrollState = stateVertical
      ) {

        Button(onClick = {
          if (isMatching) return@Button

          viewModel.performMatch(DEFAULT_SOURCE_FILE_PATH, DEFAULT_TEMPLATE_FILE_PATH)
            .onStart { isMatching = true }
            .onEach { matchResult ->
              image = matchResult.image
              resultText = "$SUCCESS\n\n${matchResult.resultText}"
            }
            .catch { resultText = MATCHING_FAILED }
            .onCompletion { isMatching = false }
            .launchIn(windowScope)

        }) {
          Text(if (isMatching) MATCHING_ELLIPSIS else START_MATCHING)
        }

        Image(bitmap = image)

        Text(text = resultText)
      }

      VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        adapter = rememberScrollbarAdapter(stateVertical)
      )
    }

  }
}
