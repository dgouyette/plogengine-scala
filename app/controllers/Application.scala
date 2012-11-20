package controllers

import play.api.mvc._
import play.api.Play.current
import play.api.cache.Cached
import utils.TextileHelper
import com.sun.syndication.io.SyndFeedOutput
import java.text.SimpleDateFormat
import play.api.data.Form
import play.api.data.Forms._
import models.{Post, Authent, Image}
import play.api.libs.openid.OpenID
import play.api.Logger
import play.api.libs.concurrent.{Thrown, Redeemed}
import com.sun.syndication.feed.synd.{SyndContentImpl, SyndEntryImpl, SyndFeedImpl}
import java.util.ArrayList


object Application extends Controller {


  val authentForm = Form(
    mapping(
      "openid_identifier" -> nonEmptyText,
      "action" -> nonEmptyText
    )(Authent.apply)(Authent.unapply)
  )


  def index(page: Int) = Action {
    implicit request =>
      val posts = Post.findAllPublished(page);
      Ok(views.html.index(posts, request.session.get("email").isEmpty))
  }


  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  def loginPost = Action {
    implicit request =>
      Form(single(
        "openid_identifier" -> nonEmptyText
      )).bindFromRequest.fold(
      error => {
        Logger.info("bad request " + error.toString)
        BadRequest(error.toString)
      }, {
        case (openid) => AsyncResult(OpenID.redirectURL(openid, routes.Application.openIDCallback.absoluteURL(), Seq("email" -> "http://schema.openid.net/contact/email", "firstName" -> "http://openid.net/schema/namePerson/first", "lastName" -> "http://openid.net/schema/namePerson/last"))
          .extend(_.value match {
          case Redeemed(url) => Redirect(url)
          case Thrown(t) => Redirect(routes.Application.login)
        }))
      }
      )
  }


  def openIDCallback = Action {
    implicit request =>
      AsyncResult(
        OpenID.verifiedId.extend(_.value match {
          case Redeemed(info) =>
            Redirect(routes.Administration.index).withSession("email" -> info.attributes.get("email").get)
          case Thrown(t) => {
            Redirect(routes.Application.login)

          }
        }))
  }

  def login = Action {
    Ok(views.html.login())
  }


  //TODO faire un redirect permanent vers l'autre methode show
  def showByDateAndUrlSimple(url: String) =
    Action {
      implicit request =>
        Post.findByUrl(url).map {
          post =>
            Post.incrementHits(post.id)
            Ok(views.html.show(post, request.session.get("email").isEmpty))
        }.getOrElse(
          NotFound("Article non trouve")
        )
    }


  def showByDateAndUrl(annee: String, mois: String, jour: String, url: String) =
    Action {
      implicit request =>
        Post.findByUrl(url).map {
          post =>
            Post.incrementHits(post.id)
            Ok(views.html.show(post, request.session.get("email").isEmpty))
        }.getOrElse(
          NotFound("Article non trouve")
        )
    }


  def fileContent(name: String) = Cached("fileContent" + name) {
    Action {
      Image.findByName(name).map {
        image =>
          Ok(image).as("image/png")
      }.getOrElse(NotFound("Oops"))

    }
  }


  def feed = Cached("feed") {
    Action {
      val feed = new SyndFeedImpl();

      feed.setFeedType("rss_2.0");
      feed.setTitle("CestPasDur.com, flux RSS");
      feed.setLink("http://www.cestpasdur.com");
      feed.setDescription("Tutoriaux et ressources du web");

      val posts = Post.findAllPublished(0, 50);
      val entries = new ArrayList[SyndEntryImpl];


      posts.items.map {
        post =>
          val entry = new SyndEntryImpl();
          entry.setTitle(post.title);
          entry.setLink(post.url);
          entry.setPublishedDate(post.postedAt);

          val description = new SyndContentImpl();
          description.setType("text/html");
          description.setValue(TextileHelper.toHtml(post.chapeau) + " ...");
          entry.setDescription(description);
          entry.setUri(routes.Application.showByDateAndUrl(new SimpleDateFormat("yyyy").format(post.postedAt), new SimpleDateFormat("MM").format(post.postedAt), new SimpleDateFormat("dd").format(post.postedAt), post.url).toString());
          entry.setLink(entry.getUri)
          entries.add(entry);
      }
      feed.setEntries(entries);
      Ok(new SyndFeedOutput().outputString(feed)).as(XML)
    }
  }


}