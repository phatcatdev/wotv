package io.phatcat.wotv.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import io.phatcat.wotv.extensions.toImageBitmap
import io.phatcat.wotv.util.Log
import io.phatcat.wotv.util.TemplateMatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.bytedeco.opencv.opencv_core.Point

class MainViewModel {
  private val templateMatching = TemplateMatching()

  fun performMatch(
    sourceFilePath: String,
    templateFilePath: String
  ): Flow<MatchResult> = flow {
    try {
      val matchResult = withContext(Dispatchers.IO) {
        val result = templateMatching.matchExact(sourceFilePath, templateFilePath)

        // This is a workaround for extracting bytes directly from the resultant matrix into an ImageBitmap
        val image = templateMatching.writeToTempFile(result.overlayMat).toImageBitmap()
        Log.d(result.points.joinToString("\n", transform = ::toFormattedString))
        MatchResult(image, result.points.size)
      }

      emit(matchResult)
    }
    catch (e: Throwable) {
      e.printStackTrace()
      error(e)
    }
  }

  data class MatchResult(
    val image: ImageBitmap,
    val numMatches: Int
  )

  private fun toFormattedString(point: Point) = "(${point.x()}, ${point.y()})"
}
