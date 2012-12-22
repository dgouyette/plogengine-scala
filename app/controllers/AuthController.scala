package controllers

import play.api.mvc.{Security, Action, Controller}
import views.html
import play.api.data.Form
import play.api.data.Forms._
import models.Account
import play.api.i18n.Messages
import play.api.Logger


object AuthController extends Controller {

  val loginForm = Form {
    mapping("email" -> email, "password" -> text)(Account.authenticate)(_.map(u => (u.email, "")))
      .verifying(Messages("authentication.invalid"), result => result.isDefined)
  }


  def authenticate = Action {
    implicit request =>
      Logger.info("authenticate")
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        user => {

          Logger.info("user = %s".format(user))
          user match {
            case Some(u) => {
              Logger.debug("Some")
              Redirect(routes.Administration.index).withSession(Security.username -> u.email)
            }
            case None => {
              Logger.debug("None")
              Forbidden
            }
          }
        }
      )
  }

  def login = Action {
    implicit request =>
      Ok(html.login(loginForm))
  }


  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "success" -> Messages("authentication.logout.success")
    )
  }

}
