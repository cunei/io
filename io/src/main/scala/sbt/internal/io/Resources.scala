/* sbt
 * Copyright 2009-2015 Typesafe, Inc, Mark Harrah, and others
 */
package sbt.internal.io

import java.io.File
import sbt.io.IO._
import Resources.error

private[sbt] object Resources {
  def apply(basePath: String): Resources =
    {
      require(basePath.startsWith("/"))
      val resource = getClass.getResource(basePath)
      if (Option(resource).isEmpty)
        error("Resource base directory '" + basePath + "' not on classpath.")
      else {
        val file = toFile(resource)
        if (file.exists)
          new Resources(file)
        else
          error("Resource base directory '" + basePath + "' does not exist.")
      }
    }
  def error(msg: String): Nothing = throw new ResourcesException(msg)
  private val LoadErrorPrefix = "Error loading initial project: "
}
private[sbt] final class ResourcesException(msg: String) extends Exception(msg)

private[sbt] final class Resources(val baseDirectory: File) {
  import Resources._
  // The returned directory is not actually read-only, but it should be treated that way
  def readOnlyResourceDirectory(group: String, name: String): File =
    {
      val groupDirectory = new File(baseDirectory, group)
      if (groupDirectory.isDirectory) {
        val resourceDirectory = new File(groupDirectory, name)
        if (resourceDirectory.isDirectory)
          resourceDirectory
        else
          error("Resource directory '" + name + "' in group '" + group + "' not found.")
      } else
        error("Group '" + group + "' not found.")
    }
  def readWriteResourceDirectory[T](group: String, name: String)(withDirectory: File => T): T =
    {
      val file = readOnlyResourceDirectory(group, name)
      readWriteResourceDirectory(file)(withDirectory)
    }

  def readWriteResourceDirectory[T](readOnly: File)(withDirectory: File => T): T =
    {
      require(readOnly.isDirectory)
      def readWrite(readOnly: File)(temporary: File): T =
        {
          val readWriteDirectory = new File(temporary, readOnly.getName)
          copyDirectory(readOnly, readWriteDirectory)
          withDirectory(readWriteDirectory)
        }
      withTemporaryDirectory(readWrite(readOnly))
    }
}
