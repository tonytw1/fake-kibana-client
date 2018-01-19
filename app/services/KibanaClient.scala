package services

import model._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KibanaClient(ws: WSClient, kibanaUrl: String, username: String, password: String) {

  val searchEndpoint = kibanaUrl + "/elasticsearch/_msearch "
  val NEW_LINE = "\r\n"

  def search(mustHaveValues: Seq[(String, String)], size: Int): Future[Option[MSearchResponse]] = {

    def buildSearchQueryFor(mustHaveValues: Seq[(String, String)]): String = {

      def matchPhrase(kv: (String, String)) = {
        Json.toJson(Map("match_phrase" -> Map(
          kv._1 -> Map(
            "query" -> kv._2
          )
        )))
      }

      val matchAll = Json.obj("match_all" -> Json.obj())

      val range = Json.obj {
        "range" -> Json.obj {
          "@timestamp" -> Json.obj(
            "gte" -> Json.toJson("now-14d"),
            "format" -> "epoch_millis"
          )
        }
      }

      val conditions = Seq(matchAll, range) ++ mustHaveValues.map(i => matchPhrase(i))

      val query = Json.toJson(Map(
        "bool" -> Json.obj(
          "must" -> conditions
        )
      )
      ).as[JsObject]

      val sort = Json.toJson(Map("@timestamp" -> Map("order" -> "desc"))).as[JsObject]

      val request = MSearchRequest(size = size, sort = Seq(sort), query)

      implicit val hr = Json.format[MSearchRequest]
      val queryString = Json.toJson(request).toString()

      val indexHeader = """{"index":["logstash-*"],"ignore_unavailable":true}"""
      Seq(indexHeader, queryString).mkString(NEW_LINE) + NEW_LINE
    }

    val q = buildSearchQueryFor(mustHaveValues)

    ws.url(searchEndpoint).
      withAuth(username, password, WSAuthScheme.BASIC).
      withHeaders(("content-type", "application/x-ndjson"), ("kbn-version", "5.5.3")).post(q).map { r =>
      if (r.status == 200) {
        implicit val hr = Json.reads[Hit]
        implicit val hsr = Json.reads[Hits]
        implicit val rsr = Json.reads[Response]
        implicit val msrr = Json.reads[MSearchResponse]
        Some(Json.parse(r.body).as[MSearchResponse])

      } else {
        None
      }
    }
  }

}
