package services

import model.Hits
import play.api.libs.json.{JsNumber, JsString, JsValue}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

trait KibanaSearch {

  def kibanaClient: KibanaClient

  def executeSearch(mustHaveValues: Seq[(String, String)], fieldsToShow: Seq[String], size: Int): Seq[Seq[String]] = {

    def collectResults(hits: Hits, fieldsToShow: Seq[String]): Seq[Seq[String]] = {
      hits.hits.map { h =>
        fieldsToShow.map { i =>
          (h._source \ i).toOption.map { j =>
            j match {
              case x: JsString => x.as[String]
              case x: JsNumber => x.value.toString()
              case x: JsValue => x.toString()
            }
          }
        }.flatten
      }
    }

    Await.result(kibanaClient.search(mustHaveValues, size), Duration(10, SECONDS)).map { r =>
      r.responses.headOption.map { hits =>
        collectResults(hits.hits, fieldsToShow)
      }.getOrElse {
        Seq()
      }
    }.getOrElse {
      Seq()
    }
  }

}
