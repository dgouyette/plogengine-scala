package controllers


import play.api.data._
import play.api.mvc._
import play.api.data.Forms._
import com.google.common.io.Files
import scala.Long
import anorm._
import java.util.Date
import java.text.SimpleDateFormat
import models._
import play.api.libs.json.{JsString, Writes, Json}
import org.joda.time.DateTime

object Administration extends Controller with Secured {


  val postForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "url" -> nonEmptyText,
      "chapeau" -> optional(nonEmptyText),
      "content" -> optional(text()),
      "hits" -> optional(longNumber),
      "postedAt" -> date("yyyy-MM-dd"),
      "published" -> boolean
    )(Post.apply)(Post.unapply)
  )

  def viderCache() = TODO


  def clearIndexes() = TODO

  def create() = withUser {
    username =>
      request =>
        Ok(views.html.administration.create(postForm))
  }

  def index() = withUser {
    username => request =>
      val posts = Post.findAll()
      val images = Image.findAll()
      Ok(views.html.administration.index(posts, images, postForm))
  }


  def delete(id: Long) = withUser {
    username =>
      request =>
        Post.delete(id)
        Redirect(routes.Administration.index())

  }

  def export = withUser {
    username =>
      request =>

        implicit val dateWrites = Writes[Date](bd => JsString(new DateTime(bd).toString("yyyy/MM/dd")))
        implicit val datetimeWrites = Writes[DateTime](bd => JsString(bd.toString("yyyy/MM/dd")))

        implicit val postWrites = Json.writes[Post]
        Ok(Json.toJson(Post.findAll())).as(JSON)

  }


  def save = withUser {
    username =>
      request =>
        implicit val req = request
        postForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.administration.create(formWithErrors)),
          post => {
            Post.create(post)
            Redirect(routes.Administration.index())
          }
        )
  }


  def imageDelete(id: Long, idArticle: Long) = withUser {
    username =>
      request =>
        implicit val req = request
        Image.deleteById(id)
        Redirect(routes.Administration.edit(idArticle))
  }

  def upload = withUser {
    username => request =>
      implicit val req = request
      val pictureBody = request.body.asMultipartFormData
      pictureBody.map {
        theFile =>
          theFile.file("picture").map {
            picture =>
              val data = Files.toByteArray(picture.ref.file)
              val image = new Image(NotAssigned, picture.contentType.get, data, picture.filename)
              Image.create(image)

              Redirect(routes.Administration.index).flashing("success" -> "Fichier ajoute");
          }.getOrElse {
            BadRequest("Probleme lors de l ajout du fichier")
          }

      }.getOrElse {
        BadRequest("Probleme lors de l ajout du fichier")
      }
  }

  def toDate(in: String): Date = {
    val sdf = new SimpleDateFormat("yyy-MM-dd");
    sdf.parse(in)
  }

  def restore = TODO

  /** Authenticated {
    (user, request) =>
      implicit val req = request




      implicit object PostReads extends Reads[LightPost] {
        def reads(json: JsValue): LightPost = {
          LightPost(
            (json \ "post" \ "title").as[String],
            (json \ "post" \ "url").as[String],
            (json \ "post" \ "chapeau").as[Option[String]],
            (json \ "post" \ "content").as[Option[String]],
            (json \ "post" \ "hits").as[Option[Long]],
            toDate((json \ "post" \ "postedAt").as[String]),
            (json \ "post" \ "published").as[Boolean]
          )


        }
      }

      val multipartFormData = request.body.asMultipartFormData
      multipartFormData.map {
        theFile =>
          theFile.file("post").map {
            post =>
              val jsValue = Json.parse(Source.fromFile(post.ref.file).mkString)

              jsValue \ "post"
              LightPost.create(jsValue.as[LightPost])
            //        Redirect(routes.Administration.index())
          }.getOrElse {
            BadRequest("Probleme lors de l ajout du fichier")
          }
      }
      Redirect(routes.Administration.index()).flashing("success"-> "fichier ajouté")
  }     **/

  def update(id: Long) = withUser {
    username =>
      request =>
        implicit val req = request
        postForm.bindFromRequest.fold(
          formWithErrors =>
            BadRequest(views.html.administration.edit(id, formWithErrors)),
          post => {
            Post.update(id, post)
            Redirect(routes.Administration.index())
          }
        )
  }

  def edit(id: Long) = withUser {
    username =>
      request =>
        Post.findById(id).map {
          post =>
            Ok(views.html.administration.edit(post.id.get, postForm.fill(post)))
        }.getOrElse(
          NotFound("404 - not found")
        )
  }


}
