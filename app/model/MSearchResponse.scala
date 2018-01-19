package model

import play.api.libs.json.JsObject

case class MSearchResponse(responses: Seq[Response])
case class Response(hits: Hits)
case class Hits(total: Long, hits: Seq[Hit])
case class Hit(_id: String, _type: String, _source: JsObject)


