package utils

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage


object TextileHelper {

  val parser = new MarkupParser(new TextileLanguage())


  def toHtml(in: Option[String]): String = {

    in match {
      case Some(str) => parser.parseToHtml(str)
      case None => ""
    }

  }


}


