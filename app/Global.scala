import models.{AccountDao, Account}
import play.api._


object Global extends GlobalSettings {

  override def onStart(app: Application) {

    Logger.info("App.start")
    Logger.info("app.mode = %s".format(app.mode))
    if (app.mode == Mode.Dev) {
      Logger.info("mode dev => creation d'un utilisateur de demo")
      AccountDao.create(Account(None, "demo@demo.fr", "demo"))
    }
  }


}