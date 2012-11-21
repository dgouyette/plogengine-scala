package utils


import models.Post
import play.api.Logger
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpHost


object ElasticClient {

  val httpclient = new DefaultHttpClient()
  val target = new HttpHost("127.0.0.1", 9200, "http");



  def indexPost(post : Post){

  }


  def clearIndex = {
    Logger.info("Index efface")


  }


}
