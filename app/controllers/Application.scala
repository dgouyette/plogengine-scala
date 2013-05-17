package controllers

import play.api.mvc._
import play.api.Play.current
import play.api.cache.Cached
import utils.TextileHelper
import com.sun.syndication.io.SyndFeedOutput
import java.text.SimpleDateFormat
import models.{PostLight, ImageDao, PostDao}
import com.sun.syndication.feed.synd.{SyndContentImpl, SyndEntryImpl, SyndFeedImpl}
import java.util.ArrayList
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders, QueryStringQueryBuilder}
import org.elasticsearch.action.search.SearchResponse


object Application extends Controller {

  val sdf = new SimpleDateFormat("yyyy-MM-dd");

  def index(page: Int) =
    Action {
      implicit request =>
        val posts = PostDao.findAllPublished(page)

        Ok(views.html.index(posts, request.session.get("email").isEmpty))
    }


  def search(q: String) = Action {
    implicit request =>
      val response = searchArticles(queryString(q))
      if (response.getHits.getHits.isEmpty) {
        val response = searchArticles(QueryBuilders.fuzzyQuery("content", q))
        Ok(views.html.search(mapResponse(response), q, response))
      } else {
        Ok(views.html.search(mapResponse(response), q, response))
      }
  }


  def mapResponse(response: SearchResponse): List[PostLight] = {
    response.getHits.getHits.map {
      h => h.getSource.keySet()
        val title = h.getSource.get("title").toString
        val url = h.getSource.get("url").toString
        val chapeau = h.getSource.get("chapeau").toString
        val content = h.getSource.get("content").toString
        val postedAt = sdf.parse(h.getSource.get("postedAt").toString)
        PostLight(title, url, Some(chapeau), Some(content), postedAt)
    }.toList
  }

  private def searchArticles(query: QueryBuilder): SearchResponse = {
    val response = Administration.client
      .prepareSearch("articles")
      .setTypes("article")
      .setQuery(query)
      .execute()
      .actionGet()
    response
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