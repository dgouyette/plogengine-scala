package models

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import java.sql.Clob
import scala.Left
import anorm.~
import anorm.TypeDoesNotMatch
import scala.Some
import scala.Right
import play.api.Logger
import org.postgresql.util.PSQLException
import play.api.i18n.Messages
import org.mindrot.jbcrypt.BCrypt

case class Account(id: Pk[Long], email: String, password: String)



object Account {
  def deleteById(accountId: Long) = {
    Logger.info("id=" + accountId)
    DB.withConnection {
      implicit connection =>
        SQL("delete from account where id={id}")
          .on('id -> accountId)
          .execute()

    }
  }




  val simple = {
    get[Pk[Long]]("id") ~
      get[String]("email") ~
      get[String]("password") map {
      case id ~ email ~ pass => Account(id, email, pass)
    }
  }

  def authenticate(email: String, password: String): Option[Account] = {
    findByEmail(email.toLowerCase.trim).filter {
      account => BCrypt.checkpw(password, account.password)
    }
  }

  def findByEmail(email: String): Option[Account] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * FROM account WHERE email = {email}").on(
          'email -> email
        ).as(simple.singleOpt)
    }
  }

  def findById(id: Long): Option[Account] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * FROM account WHERE id = {id}").on(
          'id -> id
        ).as(simple.singleOpt)
    }
  }

  def findAll: Seq[Account] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from account order by id desc").as(simple *)
    }
  }

  def update(account: Account) {
    Logger.debug("update du compte %s".format(account))
    DB.withConnection {
      implicit connection =>
        SQL("update account set email={email},password= {password} where id = {id}").on(
          'id -> account.id,
          'email -> account.email,
          'password -> BCrypt.hashpw(account.password.trim, BCrypt.gensalt())
        ).executeUpdate()
    }
  }


  def create(account: Account): Either[String, Long] = {
    Logger.debug("creation du compte %s".format(account))
    DB.withConnection {
      implicit connection =>
        try {

          val newId = SQL("select nextval('account_seq')").as(scalar[Long].single)


          SQL("INSERT INTO account (email, password) VALUES ({email}, {password})").on(
            'email -> account.email.trim,
            'password -> BCrypt.hashpw(account.password.trim, BCrypt.gensalt())
          ).executeUpdate()
          Right(newId)
        } catch {
          case e: PSQLException => {
            Logger.error("exception lors de l'insert",e)
            Left(Messages("account.existant.error"))
          }
        }
    }
  }


}