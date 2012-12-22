package models

import play.api.Play.current

import scala.slick.session.Database.threadLocalSession
import scala.slick.session.Database
import scala.slick.driver.MySQLDriver.simple._
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB


case class Account(id: Option[Long], email: String, password: String)

object AccountDao extends Table[Account]("account") {


  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")

  def password = column[String]("password")


  def * = id.? ~ email ~ password <>(Account, Account.unapply _)

  lazy val database = Database.forDataSource(DB.getDataSource())


  val byEmail = createFinderBy(_.email)


  def findByEmail(email: String) = database.withSession {
    byEmail(email).firstOption
  }


  def create(account: Account) = database.withSession {
    val accountWithPasswordEncrypted = account.copy(password = BCrypt.hashpw(account.password.trim, BCrypt.gensalt()))
    this.insert(accountWithPasswordEncrypted)
  }

  def authenticate(email: String, password: String): Option[Account] = database.withSession {
    findByEmail(email.toLowerCase.trim).filter {
      account => BCrypt.checkpw(password, account.password)
    }
  }
}
