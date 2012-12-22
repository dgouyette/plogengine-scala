package controllers

import play.api.mvc._
import models.Account
import play.api.Logger


trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = {
    Logger.info("onUnauthorized")

    //Results.Redirect(routes.Administration.index())
    Results.Redirect(routes.AuthController.login()).withNewSession
  }


  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) {
      user =>
        Action(request => f(user)(request))
    }
  }


  def withUser(f: Account => Request[AnyContent] => Result) = withAuth {
    email =>
      Logger.info("withUser %s".format(email))
      implicit request =>
        Account.findByEmail(email).map {
          Logger.info("authent ok")
          f(_)(request)
        }.getOrElse {
          Logger.info("authent ko")
          onUnauthorized(request)
        }
  }


}
