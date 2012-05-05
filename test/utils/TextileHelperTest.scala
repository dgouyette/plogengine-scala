package utils


import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._


/**
 *
 * User: damiengouyette
 */

class TextileHelperTest extends Specification {

  ".h1 titre doit donner <h1>titre</h1>" in {
    TextileHelper.toHtml("h1. Titre") must be equalTo ("<h1>titre</h1>")
  }





}
