package models


import java.sql.Date
import scala.{Long, Option}
import scala.Predef._
import play.api.db.DB

import play.api.Play.current

import scala.slick.session.Database.threadLocalSession
import scala.slick.session.Database
import scala.slick.driver.MySQLDriver.simple._


/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}


case class Post(id: Option[Long], title: String, url: String, chapeau: Option[String], content: Option[String], hits: Long, postedAt: Date, published: Boolean)


object PostDao extends Table[Post]("post") {
  def incrementHits(id: Option[Long]) = {

    id match {
      case Some(i) =>
      case None =>
    }
  }


  lazy val database = Database.forDataSource(DB.getDataSource())


  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def title = column[String]("title")

  def url = column[String]("url")

  def chapeau = column[String]("chapeau")

  def content = column[String]("content")

  def hits = column[Long]("hits")

  def postedAt = column[Date]("postedat")

  def published = column[Boolean]("published")

  val byId = createFinderBy(_.id)
  val byUrl = createFinderBy(_.url)

  def * = id.? ~ title ~ url ~ chapeau.? ~ content.? ~ hits ~ postedAt ~ published <>(Post, Post.unapply _)

  //def autoInc = id.? ~ title ~ url ~ chapeau.? ~ content.? ~ hits.? ~ postedAt ~ published <>(Post, Post.unapply _) returning id

  def findAll() = database withSession {
    (for (c <- PostDao.sortBy(_.postedAt)) yield c).list.reverse
  }

  def findAllPublished(page: Int) = database withSession {
    val posts = (for (c <- PostDao.sortBy(_.postedAt)) yield c).list.reverse
    Page(posts, page, 0, 1)

  }


  def delete(id: Long) = database withSession {
    this.where(_.id === id).delete
  }

  def create(post: Post) = database withSession {
    this.insert(post)
  }

  def update(id: Long, post: Post) = database withSession {
    this.where(_.id === id).update(post)
  }


  def findById(id: Long): Option[Post] = database withSession {
    byId(id).firstOption
  }

  def findByUrl(url: String): Option[Post] = database withSession {
    byUrl(url).firstOption
  }

}

case class Image(id: Option[Long], contenttype: String, data: Array[Byte], filename: String)


object ImageDao extends Table[Image]("image") {

  lazy val database = Database.forDataSource(DB.getDataSource())


  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def contenttype = column[String]("contenttype")

  def data = column[Array[Byte]]("data")

  def filename = column[String]("filename")


  def * = id.? ~ contenttype ~ data ~ filename <>(Image, Image.unapply _)

  def byName = createFinderBy(_.filename)

  def findAll() = {
    database.withSession {
      (for (c <- ImageDao.sortBy(_.id)) yield c).list
    }
  }

  def create(image: Image) = {
    database.withSession {
      this.insert(image)
    }
  }

  def deleteById(id: Long) = {
    database.withSession {
      this.where(_.id === id).delete
    }
  }

  def findByName(filename: String) = {
    database.withSession {
      byName(filename).firstOption
    }
  }

}
