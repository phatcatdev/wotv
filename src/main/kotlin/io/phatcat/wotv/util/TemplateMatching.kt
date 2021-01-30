package io.phatcat.wotv.util

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.opencv.global.opencv_core.CV_32FC1
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.global.opencv_imgcodecs.*
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*
import java.io.File
import java.util.*

private const val THRESHOLD_EXACT = 0.999f
private const val THRESHOLD_VERY_CLOSE = 0.95f

// Based on sample from  bytedeco / javacv
// https://github.com/bytedeco/javacv/blob/master/samples/TemplateMatching.java
// Note: There's already a TemplateMatching class we can use, have not tested it yet however.
class TemplateMatching {

  /**
   * Matches a template to a source image without applying greyscale to the images.
   * @return the resulting overlay matrix and the [Point]s used to draw the overlay
   */
  fun matchExact(
    sourceImagePath: String,
    templateImagePath: String,
    threshold: Float = THRESHOLD_EXACT,
  ): Result {
    val sourceColor: Mat = imread(sourceImagePath)
    val template: Mat = imread(templateImagePath)

    // Size for the result image
    val templateWidth = template.cols() + 1
    val templateHeight = template.rows() + 1
    val size = Size(sourceColor.cols() - templateWidth, sourceColor.rows() - templateHeight)
    val result = Mat(size, CV_32FC1)
    matchTemplate(sourceColor, template, result, TM_CCORR_NORMED)

    val points = getPointsFromMatAboveThreshold(result, threshold)
    points.forEach { point ->
      rectangle(sourceColor, Rect(point.x(), point.y(), template.cols(), template.rows()), MATCH_COLOR, 2, 0, 0)
    }

    // We'll grab the upper left coordinate only
    val groupedPoints = points.windowed(
      size = templateWidth,
      step = templateWidth
    ).map(List<Point>::first)

    return Result(sourceColor, groupedPoints)
  }

  /**
   * Matches a template to a source image applying greyscale to the images.
   * @return the resulting overlay matrix and the [Point]s used to draw the overlay
   */
  fun match(
    sourceImagePath: String,
    templateImagePath: String,
    threshold: Float = THRESHOLD_VERY_CLOSE,
  ): Result {
    val sourceColor: Mat = imread(sourceImagePath)
    val sourceGrey = Mat(sourceColor.size(), CV_8UC1)
    cvtColor(sourceColor, sourceGrey, COLOR_BGR2GRAY)

    // Load in template in grey to match
    val template: Mat = imread(templateImagePath, IMREAD_GRAYSCALE)

    // Size for the result image
    val size = Size(sourceGrey.cols() - template.cols() + 1, sourceGrey.rows() - template.rows() + 1)
    val result = Mat(size, CV_32FC1)
    matchTemplate(sourceGrey, template, result, TM_CCORR_NORMED)

    // May want adaptive instead for images that are shaded slightly differently
//    threshold(result, result, 0.1, 1.0, THRESH_TOZERO)

    val points = getPointsFromMatAboveThreshold(result, threshold)
    points.forEach { point ->
      rectangle(sourceColor, Rect(point.x(), point.y(), template.cols(), template.rows()), MATCH_COLOR, 2, 0, 0)
    }

    return Result(sourceColor, points)
  }

  fun writeToTempFile(mat: Mat, fileName: String = DEFAULT_RESULT_FILE_PATH): File {
    imwrite(fileName, mat)
    return File(fileName).apply {
      deleteOnExit()
    }
  }

  private fun getPointsFromMatAboveThreshold(m: Mat, t: Float): List<Point> {
    val matches = ArrayList<Point>()
    val indexer: FloatIndexer = m.createIndexer()
    for (y in 0 until m.rows()) {
      for (x in 0 until m.cols()) {
        val yL = y.toLong()
        val xL = x.toLong()
        if (indexer.get(yL, xL) > t) {
          println("(" + x + "," + y + ") = " + indexer.get(yL, xL))
          matches.add(Point(x, y))
        }
      }
    }
    return matches
  }

  data class Result(
    val overlayMat: Mat,
    val points: List<Point>,
  )

  companion object {
    private val MATCH_COLOR = Scalar(0.0, 0.0, 255.0, 0.0) // Red
    private const val DEFAULT_RESULT_FILE_PATH = "result-tmp.png"
  }
}
