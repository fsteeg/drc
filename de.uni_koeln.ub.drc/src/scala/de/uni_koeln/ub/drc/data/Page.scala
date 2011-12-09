/**
 * ************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * ***********************************************************************************************
 */

package de.uni_koeln.ub.drc.data

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import scala.collection.mutable.ListBuffer
import com.quui.sinist.XmlDb
import scala.xml._
import java.io._
import java.util.zip._
import de.uni_koeln.ub.drc.reader.Point

/**
 * Representation of a scanned page.
 * @param words The list of words this page consists of
 * @param id An ID for this page (TODO: update to e.g. URI)
 * @author Fabian Steeg
 */
case class Page(words: List[Word], id: String) {

  def volume: String = { val Page.VolumePageExtractor(vol, _) = id; vol }
  def number: Int = { val Page.VolumePageExtractor(_, num) = id; num.toInt }
  def edits = (0 /: words)(_ + _.history.size) - words.size

  val tags: ListBuffer[Tag] = new ListBuffer()
  val comments: ListBuffer[Comment] = new ListBuffer()
  val status: ListBuffer[Status] = new ListBuffer()

  var imageBytes: Option[Array[Byte]] = None

  def toXml =
    <page id={ id }>
      { words.map(_.toXml) }
      { tags.map(_.toXml) }
      { comments.map(_.toXml) }
      { status.map(_.toXml) }
    </page>

  def toText(delim: String) =
    ("" /: words)(_ + " " + _.history.top.form.replace(Page.ParagraphMarker, delim))

  def format(root: Node) = {
    val formatted = new StringBuilder
    new PrettyPrinter(120, 2).format(root, formatted)
    formatted
  }

  def saveToDb(collection: String = Index.DefaultCollection, db: XmlDb): Node = {
    val file = id.split("/").last
    val c = collection + "/" + file.split("-")(0)
    val entry = file
    val dbRes = db.getXml(c, entry)
    val mergedPage = mergedDbVersion(dbRes, entry)
    val root = mergedPage.toXml
    val formatted = format(root)
    db.putXml(root, c, entry)
    root
  }

  def mergedDbVersion(dbRes: Option[List[Node]], entry: String) = dbRes match {
    case None => this // no merging needed
    case Some(res) => {
      val dbEntry = Page.fromXml(res(0))
      Page.mergePages(this, dbEntry)
    }
  }

  def done = status.size > 0 && status.last.finished

}

object Page {

  // PPN345572629_0014_02-0001.xml or PPN345572629_0004-0001.xml
  val VolumePageExtractor = """[^_]+_([^\-]+)-([^\.]+)\.xml""".r
  val ParagraphMarker = "@"

  def fromXml(page: Node, id: String = ""): Page = {
    val p = Page(for (word <- (page \ "word").toList) yield Word.fromXml(word), if (id == "") (page \ "@id").text else id)
    for (tag <- (page \ "tag")) p.tags += Tag.fromXml(tag)
    for (comment <- (page \ "comment")) p.comments += Comment.fromXml(comment)
    for (status <- (page \ "status")) p.status += Status.fromXml(status)
    p
  }

  def fromPdf(pdf: String): Page = { PdfToPage.convert(pdf) }

  /**
   * This models what we get from the OCR: the original word forms as recognized by the OCR,
   * together with their coordinates in the scan result (originally a PDF with absolute values).
   */
  private val map = Map(
    "daniel" -> Box(130, 283, 150, 30),
    "bonifaci" -> Box(280, 285, 180, 30),
    "catechismus" -> Box(70, 330, 80, 20),
    "als" -> Box(110, 390, 30, 20),
    "slaunt" -> Box(78, 498, 45, 15))

  /**
   * This models the other part we get from the OCR: the full text, which we need to tokenize and
   * convert into Word objects to be displayed and edited in the UI.
   */
  val mock: Page =
    Page(
      for (w <- "Daniel Bonifaci Catechismus Als Slaunt".split(" ").toList)
        yield Word(w, map(w.toLowerCase)), "testing-mock")

  /**
   * @param lists The lists of pages to merge (each independently edited, e.g. by different users)
   * @return A single list of pages containing the merged content
   */
  def merge(lists: List[Page]*): Seq[Page] = {
    for (p1 <- lists.head; list2 <- lists.tail; p2 <- list2; if p1.id == p2.id)
      mergePages(p1, p2)
    lists.head
  }

  private def mergePages(p1: Page, p2: Page): Page = {
    for (
      w1 <- p1.words; w2 <- p2.words; m <- w2.history.reverse; // TODO ID for words?
      if (w1.original == w2.original && w1.position == w2.position && (!w1.history.contains(m)))
    ) {
      w1.history.push(m)
    }
    p1
  }

}

private[data] case class Comment(user: String, text: String, date: Long) {
  def toXml = <comment user={ user } date={ date.toString }>{ text }</comment>
}

private[data] object Comment {
  def fromXml(xml: Node) = Comment((xml \ "@user").text, xml.text, (xml \ "@date").text.toLong)
}

private[data] case class Status(user: String, date: Long, finished: Boolean) {
  def toXml = <status user={ user } date={ date.toString } finished={ finished.toString }></status>
}

private[data] object Status {
  def fromXml(xml: Node) = Status((xml \ "@user").text, (xml \ "@date").text.toLong, (xml \ "@finished").text.toBoolean)
}

private[data] case class Tag(text: String, user: String) {
  def toXml = <tag user={ user } text={ text }/>
  override def toString = text
}

private[data] object Tag {
  def fromXml(xml: Node) = Tag((xml \ "@text").text, (xml \ "@user").text)
}

/**
 *  Experimental heuristics for creating an XML page representation from a scanned PDF.
 *  Includes computation of the highlighting box based on line start coordinated read from the PDF.
 *  @author Fabian Steeg (fsteeg)
 */
private object PdfToPage {

  import java.net.URL
  import de.uni_koeln.ub.drc.reader._
  import scala.collection.JavaConversions._
  import scala.collection.mutable.Buffer
  import java.io.File

  def convert(pdfLocation: String): Page = {
    val words: Buffer[Word] = Buffer()
    val paragraphs: Buffer[Paragraph] = PdfContentExtractor.extractContentFromPdf(pdfLocation).getParagraphs
    val pdf = new java.io.File(pdfLocation)
    val png = pdf.getName.replace(" ", "").replace("pdf", "png")
    val pngFile = new java.io.File(pdf.getParent, png)
    val bufferedImage = ImageIO.read(pngFile)
    val pageHeight = bufferedImage.getHeight()
    val pageWidth = bufferedImage.getWidth()
    for (p <- paragraphs) {
      for (word <- p.getWords) {
        var startPos = word.getStartPointScaled(pageWidth, pageHeight)
        var endPos = word.getEndPointScaled(pageWidth, pageHeight)
        val scaled = word.getFontSizeScaled(pageHeight)
        val wordWidth = endPos.getX - startPos.getX //width(word.getText, scaled) 
        words add Word(word.getText, Box(startPos.getX.toInt, startPos.getY.toInt - scaled, wordWidth.toInt, scaled))
      }
      words add Word(Page.ParagraphMarker, Box(0, 0, 0, 0))
    }
    Page(words.toList, new java.io.File(pdfLocation).getName().replace(" ", "").replace(".pdf", ".xml"))
  }
}
