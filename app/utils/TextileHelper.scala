package utils


import org.clapper.markwrap._

object TextileHelper {

  def toHtml(in: Option[String]): String = {
    val parser = MarkWrap.parserFor(MarkupType.Textile)
    in.map(in => parser.parseToHTML(in)).getOrElse("")
  }


}


