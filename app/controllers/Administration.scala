package controllers

import anorm.{Pk, NotAssigned}


import play.api.data._
import play.api.mvc._
import scala.Long
import play.api.data.Forms._
import com.google.common.io.Files
import models.{Post, Image, User}

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


  def create = Authenticated {
    (user, request) =>
      Ok(views.html.administration.create(postForm))
  }

  def index = Authenticated {
    (user, request) =>
      val posts = Post.findAll();
      Ok(views.html.administration.index(posts))
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

  def upload(id: Long) = Authenticated {
    (user, request) =>
      implicit val req = request
      val textBody = request.body.asMultipartFormData
      textBody.map {
        theFile =>
          theFile.file("picture").map {
            picture =>
              val data = Files.toByteArray(picture.ref.file)
              val image = new Image(data, id, picture.contentType.get, picture.filename)
              Image.create(image)
              Redirect(routes.Administration.index).flashing("success" -> "Fichier ajoute");
          }.getOrElse {
            BadRequest("Probleme lors de l ajout du fichier")
          }

      }.getOrElse {
        BadRequest("Probleme lors de l ajout du fichier")
      }
  }

  def update(id: Long) = Authenticated {
    (user, request) =>
      implicit val req = request
      postForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.administration.edit(id, formWithErrors)),
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
