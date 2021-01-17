package io.phatcat.wotv.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import io.phatcat.wotv.extensions.toImageBitmap
import io.phatcat.wotv.util.TemplateMatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel {
  private val templateMatching = TemplateMatching()

  fun performMatch(
    sourceFilePath: String,
    templateFilePath: String
  ): Flow<ImageBitmap> = flow {
    try {
      val file: File = withContext(Dispatchers.IO) {
        val result = templateMatching.matchExact(sourceFilePath, templateFilePath)

        // This is a workaround for extracting bytes directly from the resultant matrix into an ImageBitmap
        templateMatching.writeToTempFile(result.overlayMat)
      }
      emit(file.toImageBitmap())
    }
    catch (e: Throwable) {
      e.printStackTrace()
      error(e)
    }
  }

  data class MainUiModel(
    val isMatching: Boolean,
    val resultText: String,
    val image: ImageBitmap
  )
}
