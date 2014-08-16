package controllers


import play.api.data._
import play.api.mvc._
import play.api.data.Forms._
import com.google.common.io.Files
import scala.Long
import java.util.Date
import models._
import play.api.libs.json.{JsString, Writes, Json}
import models.Image
import models.Post
import org.joda.time.DateTime

import org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress


object Administration extends Controller with Secured {


  val client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("hotel-village-soleil.com", 9300))
  val dateFormat = "dd-MM-yyyy"

  val postForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "url" -> nonEmptyText,
      "chapeau" -> optional(nonEmptyText),
      "content" -> optional(text()),
      "hits" -> optional(longNumber),
      "postedAt" -> sqlDate(dateFormat),
      "published" -> boolean
    )(Post.apply)(Post.unapply)
  )

  def viderCache() = TODO


  def indexSearch = withUser {
    username =>
      request =>
        PostDao.findAll().map {
          post =>
            if (post.published) {
              client.prepareIndex("articles", "article").setSource(jsonBuilder()
                .startObject()
                .field("chapeau", post.chapeau.getOrElse(""))
                .field("url", post.url)
                .field("postedAt", post.postedAt)
                .field("title", post.title)
                .field("content", post.content.getOrElse("")).endObject()
              ).execute().actionGet()
            }
        }
        Ok(s"index ok")

  }


  def restore() = Action(parse.json) {
    request =>
      implicit val postReads = Json.reads[Post]

      val articleJson = request.body
      val post = articleJson.as[Post]
      PostDao.create(post)
      Ok(s" post.title = ${post.title} restauré")
  }


  def export = withUser {
    username =>
      request =>

        implicit val dateWrites = Writes[Date](bd => JsString(new DateTime(bd).toString(dateFormat)))
        implicit val datetimeWrites = Writes[DateTime](bd => JsString(bd.toString(dateFormat)))

        implicit val postWrites = Json.writes[Post]
        Ok(Json.toJson(PostDao.findAll())).as(JSON)

  }


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
            NotFound
          )
  }


}
