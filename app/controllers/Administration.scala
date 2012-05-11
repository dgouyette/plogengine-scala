package controllers

import anorm.{Pk, NotAssigned}


import play.api.data._
import play.api.mvc._
import play.api.data.Forms._
import com.google.common.io.Files
import scala.Long
import io.Source
import play.api.libs.json.{Reads, JsValue, Json}
import anorm._
import java.util.Date
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import org.joda.time.format.DateTimeFormat
import models.{LightPost, Post, Image, User}
import play.api.cache.Cache


//import org.apache.commons.io.FileUtils


/**
 *
 * User: damiengouyette
 */

object Administration extends Controller {

  case class AuthenticatedRequest(val user: User, request: Request[AnyContent]
                                   ) extends WrappedRequest(request)


  def Authenticated(f: (User, Request[AnyContent]) => Result) = {
    Action {
      request =>
        request.session.get("email").flatMap(u => User.findByEmail(u)).map {
          user =>
            f(user, request)
        }.getOrElse(Unauthorized("401 - Unauthorized"))
    }
  }


  val postForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: Pk[Long]),
      "title" -> nonEmptyText,
      "url" -> nonEmptyText,
      "chapeau" -> optional(nonEmptyText),
      "content" -> optional(text()),
      "hits" -> optional(longNumber),
      "postedAt" -> date("yyyy-MM-dd"),
      "published" -> boolean
    )(Post.apply)(Post.unapply)
  )

  def viderCache={

  }


  def create = Authenticated {
    (user, request) =>
      Ok(views.html.administration.create(postForm))
  }

  def index = Authenticated {
    (user, request) =>
      val posts = Post.findAll()
      val images = Image.findAll()
      Ok(views.html.administration.index(posts, images, postForm))
  }


  def delete(id: Long) = Authenticated {
    (user, request) =>
      Post.delete(id)
      Redirect(routes.Administration.index())

  }


  def save = Authenticated {
    (user, request) =>
      implicit val req = request
      postForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.administration.create(formWithErrors)),
        post => {
          Post.create(post)
          Redirect(routes.Administration.index())
        }
      )
  }


  def imageDelete(id: Long, idArticle: Long) = Authenticated {
    (user, request) =>
      implicit val req = request
      Image.deleteById(id)
      Redirect(routes.Administration.edit(idArticle))
  }

  def upload = Authenticated {
    (user, request) =>
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

  //id: Pk[Long], title: String, url: String, chapeau: Option[String], content: Option[String], hits: Option[Long], postedAt: Date, published: Boolean
  def restore = Authenticated {
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
      Redirect(routes.Administration.index())
  }

  def update(id: Long) = Authenticated {
    (user, request) =>
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

  def edit(id: Long) = Authenticated {
    (user, request) =>


      Post.findById(id).map {
        post =>
          Ok(views.html.administration.edit(post.id.get, postForm.fill(post)))
      }.getOrElse(
        NotFound("Not found")
      )
  }


}
