package sbt.internal.io

import java.io.Writer

/** A `Writer` that avoids constructing the underlying `Writer` with `make` until a method other than `close` is called on this `Writer`. */
private[sbt] final class DeferredWriter(make: => Writer) extends Writer {
  private[this] var opened = false
  private[this] var delegate0: Writer = _
  private[this] def delegate: Writer = synchronized {
    if (Option(delegate0).isEmpty) {
      delegate0 = make
      opened = true
    }
    delegate0
  }
  override def close(): Unit = synchronized {
    if (opened) delegate0.close()
  }

  override def append(c: Char): Writer = delegate.append(c)
  override def append(csq: CharSequence): Writer = delegate.append(csq)
  override def append(csq: CharSequence, start: Int, end: Int): Writer = delegate.append(csq, start, end)
  override def flush(): Unit = delegate.flush()
  override def write(cbuf: Array[Char]): Unit = delegate.write(cbuf)
  override def write(cbuf: Array[Char], off: Int, len: Int): Unit = delegate.write(cbuf, off, len)
  override def write(c: Int): Unit = delegate.write(c)
  override def write(s: String): Unit = delegate.write(s)
  override def write(s: String, off: Int, len: Int): Unit = delegate.write(s, off, len)
}
