package controllers


import play.api.data._
import play.api.mvc._
import play.api.data.Forms._
import com.google.common.io.Files
import scala.Long
import java.util.Date
import java.text.SimpleDateFormat
import models._
import play.api.libs.json.Json
import scala.Some
import models.Image
import models.Post

object Administration extends Controller with Secured {


  val postForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "url" -> nonEmptyText,
      "chapeau" -> optional(nonEmptyText),
      "content" -> optional(text()),
      "hits" -> optional(longNumber),
      "postedAt" -> sqlDate("yyyy-MM-dd"),
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
      val posts = PostDao.findAll()
      val images = ImageDao.findAll()
      Ok(views.html.administration.index(posts, images, postForm))
  }


  def export() = withUser {
    username => request =>
      implicit val postWrites = Json.writes[Post]
      val posts = PostDao.findAll()
      Ok(Json.toJson(posts)).as(JSON)
  }


  def delete(id: Long) = withUser {
    username =>
      request =>
        PostDao.delete(id)
        Redirect(routes.Administration.index()).flashing("success" -> "delete.success");

  }


  def save = withUser {
    username =>
      request =>
        implicit val req = request
        postForm.bindFromRequest.fold(
          formWithErrors => BadRequest(formWithErrors.errorsAsJson),
          post => {
            PostDao.create(post)
            Redirect(routes.Administration.index()).flashing("success" -> "save.success");
          }
        )
  }


  def imageDelete(id: Long, idArticle: Long) = withUser {
    username =>
      request =>
        implicit val req = request
        ImageDao.deleteById(id)
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
              val image = new Image(None, picture.contentType.get, data, picture.filename)
              ImageDao.create(image)

              Redirect(routes.Administration.index).flashing("success" -> "Fichier ajoute");
          }.getOrElse {
            BadRequest("Probleme lors de l ajout du fichier")
          }

      }.getOrElse {
        BadRequest("Probleme lors de l ajout du fichier")
      }
  }


  def restore = TODO


  def update(id: Long) = withUser {
    username =>
      request =>
        implicit val req = request
        postForm.bindFromRequest.fold(
          formWithErrors =>
            BadRequest(views.html.administration.edit(id, formWithErrors)),
          post => {
            PostDao.update(id, post)
            Redirect(routes.Administration.index())
          }
        )
  }

  def edit(id: Long) = withUser {
    username =>
      request =>
        PostDao.findById(id).map {
          post =>
            Ok(views.html.administration.edit(post.id.get, postForm.fill(post)))
        }.getOrElse(
          NotFound("404 - not found")
        )
  }


}
