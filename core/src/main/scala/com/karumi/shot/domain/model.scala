package com.karumi.shot.domain

import com.karumi.shot.domain.model.{FilePath, ScreenshotComparisionErrors, ScreenshotsSuite}

import java.io.File

object model {
  type ScreenshotsSuite            = Seq[Screenshot]
  type FilePath                    = String
  type Folder                      = String
  type AppId                       = String
  type ScreenshotComparisionErrors = Seq[ScreenshotComparisonError]
}

object Config {
  val defaultTolerance: Double         = 0.0
  val shotConfiguration: String        = "shotDependencies"
  val androidDependencyMode: FilePath  = "androidTestImplementation"
  val androidDependencyGroup: String   = "com.karumi"
  val androidDependencyName: String    = "shot-android"
  val androidDependencyVersion: String = "5.11.2"
  val androidDependency: FilePath =
    s"$androidDependencyGroup:$androidDependencyName:$androidDependencyVersion"

  def screenshotsFolderName(flavor: String, buildType: String): FilePath =
    if (flavor.isEmpty) {
      normalizedPath(s"/screenshots/$buildType/")
    } else {
      normalizedPath(s"/screenshots/$flavor/$buildType/")
    }

  def pulledScreenshotsFolder(flavor: String, buildType: String): FilePath =
    normalizedPath(screenshotsFolderName(flavor, buildType) + "screenshots-default/")

  def pulledComposeScreenshotsFolder(flavor: String, buildType: String): FilePath =
    normalizedPath(screenshotsFolderName(flavor, buildType) + "screenshots-compose-default/")

  def metadataFileName(flavor: String, buildType: String): FilePath =
    normalizedPath(pulledScreenshotsFolder(flavor, buildType) + "metadata.xml")

  def composeMetadataFileName(flavor: String, buildType: String): FilePath =
    normalizedPath(pulledComposeScreenshotsFolder(flavor, buildType) + "metadata.json")

  val androidPluginName: FilePath           = "com.android.application"
  val screenshotsTemporalRootPath: FilePath = normalizedPath("/tmp/shot/screenshot/")

  def defaultInstrumentationTestTask(flavor: String, buildType: String): String =
    s"connected${flavor.capitalize}${buildType.capitalize}AndroidTest"

  def composerInstrumentationTestTask(flavor: String, buildType: String) =
    s"test${flavor.capitalize}${buildType.capitalize}Composer"

  val defaultPackageTestApkTask: String = "packageDebugAndroidTest"

  def reportFolder(flavor: String, buildType: String): String = normalizedPath("/reports/shot")

  def verificationReportFolder(flavor: String, buildType: String): String =
    normalizedPath(reportFolder(flavor, buildType) + "/verification")

  def recordingReportFolder(flavor: String, buildType: String): String =
    normalizedPath(reportFolder(flavor, buildType) + "/record")

  val defaultTaskName: String = "executeScreenshotTests"


  private def normalizedPath(path: FilePath): FilePath = {
    path.replace("/", File.separator)
  }
}

case class Screenshot(
    name: String,
    recordedScreenshotPath: String,
    temporalScreenshotPath: String,
    testClass: String,
    testName: String,
    tilesDimension: Dimension,
    viewHierarchy: FilePath,
    absoluteFileNames: Seq[FilePath],
    relativeFileNames: Seq[FilePath],
    recordedPartsPaths: Seq[FilePath],
    screenshotDimension: Dimension
) {
  val fileName: String =
    temporalScreenshotPath.substring(
      temporalScreenshotPath.lastIndexOf("/") + 1,
      temporalScreenshotPath.length
    )

  def getDiffScreenshotPath(basePath: String): String =
    s"${basePath}diff_$fileName"

}

case class Dimension(width: Int, height: Int) {
  val isZero: Boolean = width == 0 && height == 0

  override def toString: FilePath = width + "x" + height
}

sealed trait ScreenshotComparisonError {
  def errorScreenshot: Screenshot =
    this match {
      case ScreenshotNotFound(screenshot)             => screenshot
      case DifferentScreenshots(screenshot, _)        => screenshot
      case DifferentImageDimensions(screenshot, _, _) => screenshot
    }
}

case class ScreenshotNotFound(screenshot: Screenshot) extends ScreenshotComparisonError

case class DifferentScreenshots(screenshot: Screenshot, base64Diff: Option[String] = None)
    extends ScreenshotComparisonError

case class DifferentImageDimensions(
    screenshot: Screenshot,
    originalDimension: Dimension,
    newDimension: Dimension
) extends ScreenshotComparisonError

case class ScreenshotsComparisionResult(
    errors: ScreenshotComparisionErrors = Seq(),
    screenshots: ScreenshotsSuite = Seq()
) {
  val hasErrors: Boolean                = errors.nonEmpty
  val errorScreenshots: Seq[Screenshot] = errors.map(_.errorScreenshot)
  val correctScreenshots: Seq[Screenshot] =
    screenshots.filterNot(errorScreenshots.contains(_))
}
