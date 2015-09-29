/* sbt -- Simple Build Tool
 * Copyright 2009  Mark Harrah
 */
package sbt.internal.io

import java.io.IOException

private[sbt] object ErrorHandling {
  def translate[T](msg: => String)(f: => T): T =
    try { f }
    catch {
      case e: IOException => throw new TranslatedIOException(msg + e.toString, e)
      case e: Exception   => throw new TranslatedException(msg + e.toString, e)
    }

  def wideConvert[T](f: => T): Either[Throwable, T] =
    try { Right(f) }
    catch {
      case ex @ (_: Exception | _: StackOverflowError)     => Left(ex)
      case err @ (_: ThreadDeath | _: VirtualMachineError) => throw err
      case x: Throwable                                    => Left(x)
    }

  def convert[T](f: => T): Either[Exception, T] =
    try { Right(f) }
    catch { case e: Exception => Left(e) }

  def reducedToString(e: Throwable): String =
    if (e.getClass == classOf[RuntimeException]) {
      val msg = e.getMessage
      if (Option(msg).isEmpty || msg.isEmpty) e.toString
      else msg
    } else e.toString
}
private[sbt] sealed class TranslatedException private[sbt] (msg: String, cause: Throwable) extends RuntimeException(msg, cause) {
  override def toString: String = msg
}
private[sbt] final class TranslatedIOException private[sbt] (msg: String, cause: IOException) extends TranslatedException(msg, cause)
