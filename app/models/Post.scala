package models


import play.api.Play.current
import anorm.SqlParser._


import java.util.Date
import play.api.Logger
import play.api.db.DB
import scala.{Long, Option}
import anorm._
import scala.Predef._


case class Post(id: Pk[Long], title: String, url: String, chapeau: Option[String], content: Option[String], hits: Option[Long], postedAt: Date, published: Boolean)

case class LightPost(title: String, url: String, chapeau: Option[String], content: Option[String], hits: Option[Long], postedAt: Date, published: Boolean)

case class PostBackup(post: LightPost, images: Seq[Image])

case class Image(id: Pk[Long], contenttype: String, data: Array[Byte], filename: String)

case class Authent(openid_identifier: String, action: String)

case class User(id: Pk[Long], firstName: String, lastName: String, courriel: String)


/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object LightPost {
  def create(post: LightPost) = {
    Post.create(Post(NotAssigned, post.title, post.url, post.chapeau, post.content, post.hits, post.postedAt, post.published))
  }
}

object Post {
  def incrementHits(postId: Pk[Long]){
    DB.withConnection {
      implicit connection =>
        SQL("update post set hits=hits+1 where id = {id}")
          .on('id -> postId)
          .executeUpdate()
    }
  }


  def findById(id: Long): Option[Post] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from Post where id = {id}")
          .on('id -> id)
          .as(simple.singleOpt)
    }
  }


  val simple = {
    get[Pk[Long]]("id") ~
      get[String]("title") ~
      get[String]("url") ~
      get[Option[String]]("chapeau") ~
      get[Option[String]]("content") ~
      get[Option[Long]]("hits") ~
      get[Date]("postedAt") ~
      get[Boolean]("published") map {
      case id ~ title ~ url ~ chapeau ~ content ~ hits ~ postedAt ~ published => Post(id, title, url, chapeau, content, hits, postedAt, published)
    }
  }


  def findByUrl(url: String): Option[Post] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from Post where url = {url}")
          .on('url -> url)
          .as(simple.singleOpt)
    }
  }

  def findAllPublished(page: Int = 0, pageSize: Int = 10): Page[Post] = {

    val offset = pageSize * page

    DB.withConnection {
      implicit connection =>
        val posts = SQL("select * from post where published = true order by postedAt  desc limit {pageSize} offset {offset}")
          .on(
          'pageSize -> pageSize,
          'offset -> offset).as(Post.simple *)

        val totalRows = SQL(
          """
            select count(*) from Post
            where published = 'true'
          """
        ).as(scalar[Long].single)

        Page(posts, page, offset, totalRows)

    }
  }

  def findAll(): Seq[Post] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from Post order by postedAt desc").as(simple *)
    }
  }


  def delete(id: Long) = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from post where id = {id}").on('id -> id).executeUpdate()
    }
  }

  def create(post: Post) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into post
            (title, url, chapeau, content, hits, postedAt, published)
              values
            ({title}, {url}, {chapeau}, {content}, {hits}, {postedAt}, {published})
          """
        ).on(
          'url -> post.url,
          'title -> post.title,
          'chapeau -> post.chapeau,
          'content -> post.content,
          'hits -> post.hits,
          'published -> post.published,
          'postedAt -> post.postedAt
        ).executeInsert()
    }
  }


  def update(id: Long, post: Post) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
            update post
            set
              title = {title}, url= {url}, chapeau = {chapeau}, content = {content},   postedAt = {postedAt},
              published = {published}
            where id = {id}
          """
        ).on(
          'id -> id,
          'url -> post.url,
          'title -> post.title,
          'chapeau -> post.chapeau,
          'content -> post.content,
          'published -> post.published,
          'postedAt -> post.postedAt
        ).executeUpdate()
    }
  }


}


object User {


  val simpleUser = {
    get[Pk[Long]]("id") ~
      get[String]("courriel") ~
      get[String]("firstName") ~
      get[String]("lastName") map {
      case id ~ courriel ~ firstName ~ lastName => User(id, courriel, firstName, lastName)
    }
  }


  def findByEmail(email: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from utilisateur  WHERE courriel = {param}")
          .on('param -> email)
          .as(simpleUser.singleOpt)
    }
  }

}


object Image {

  implicit val rowToByteArray: Column[Array[Byte]] = new Column[Array[Byte]] {
    def apply(value: Any, metaData: MetaDataItem) = value match {
      case o: Array[Byte] => Right(o)
      case _ => Left(TypeDoesNotMatch("Oops"))
    }
  }

  val simpleByte = {
    get[Array[Byte]]("data")
  }


  val simple = {
    get[Pk[Long]]("id") ~
      get[String]("contenttype") ~
      get[Array[Byte]]("data") ~
      get[String]("filename") map {
      case id ~ contenttype ~ data ~ filename => Image(id, contenttype, data, filename)
    }

  }


  def deleteById(id: Long) = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from image where id = {id}").on('id -> id).executeUpdate()
    }
  }


  def create(image: Image) = {
    Logger.info("CREATE [" + image + "]")
    DB.withConnection {
      implicit connection =>
        SQL( """
        insert into image
         (data, contenttype, fileName)
         values
         ({data},  {contenttype}, {filename})
             """)
          .on('data -> image.data,
          'contenttype -> image.contenttype,
          'filename -> image.filename
        ).executeUpdate()
    }
  }

  def findAll() = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from image order by filename").as(simple.*)
    }
  }

  def findByName(name: String): Option[Array[Byte]] = {

    Logger.debug("findByName " + name)

    DB.withConnection {
      implicit connection =>

        SQL("select * from image  where filename = {name}")
          .on('name -> name)
          .as(simpleByte.singleOpt)

    }
  }


}
