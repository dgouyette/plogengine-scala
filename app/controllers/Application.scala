package controllers

import play.api.mvc._
import play.api.Play.current
import play.api.cache.Cached
import utils.TextileHelper
import com.sun.syndication.io.SyndFeedOutput
import java.text.SimpleDateFormat
import models.{ImageDao, PostDao, Post, Image}
import com.sun.syndication.feed.synd.{SyndContentImpl, SyndEntryImpl, SyndFeedImpl}
import java.util.ArrayList
import play.api._


object Application extends Controller {


  def index(page: Int) =
    Action {
      implicit request =>
        val posts = PostDao.findAllPublished(page)

        Ok(views.html.index(posts, request.session.get("email").isEmpty))
    }


  //TODO faire un redirect permanent vers l'autre methode show
  def showByDateAndUrlSimple(url: String) =
    Action {
      implicit request =>
        PostDao.findByUrl(url).map {
          post =>
            Ok(views.html.show(post, request.session.get("email").isEmpty))
        }.getOrElse(
          NotFound("Article non trouve")
        )
    }


  def showByDateAndUrl(annee: String, mois: String, jour: String, url: String) =
    Action {
      implicit request =>
        PostDao.findByUrl(url).map {
          post =>
            Ok(views.html.show(post, request.session.get("email").isEmpty))
        }.getOrElse(
          NotFound("Article non trouve")
        )
    }


  def fileContent(name: String) = Cached("fileContent" + name) {
    Action {
      ImageDao.findByName(name).map {
        image =>
          Ok(image.data).as(image.contenttype)
      }.getOrElse(NotFound(name))

    }
  }


  def feed = Cached("feed") {
    Action {
      val feed = new SyndFeedImpl()

      feed.setFeedType("rss_2.0")
      feed.setTitle("CestPasDur.com, flux RSS")
      feed.setLink("http://www.cestpasdur.com")
      feed.setDescription("Tutoriaux et ressources du web")

      val posts = PostDao.findAllPublished(0)
      val entries = new ArrayList[SyndEntryImpl]


      posts.items.map {
        post =>
          val entry = new SyndEntryImpl()
          entry.setTitle(post.title)
          entry.setLink(post.url)
          entry.setPublishedDate(post.postedAt)

          val description = new SyndContentImpl()
          description.setType("text/html")
          description.setValue(TextileHelper.toHtml(post.chapeau) + " ...")
          entry.setDescription(description)
          entry.setUri(routes.Application.showByDateAndUrl(new SimpleDateFormat("yyyy").format(post.postedAt), new SimpleDateFormat("MM").format(post.postedAt), new SimpleDateFormat("dd").format(post.postedAt), post.url).toString())
          entry.setLink(entry.getUri)
          entries.add(entry)
      }
      feed.setEntries(entries)
      Ok(new SyndFeedOutput().outputString(feed)).as(XML)
    }
  }


}