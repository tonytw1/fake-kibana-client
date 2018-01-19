package model

import play.api.libs.json.JsObject

case class MSearchRequest(size: Int, sort: Seq[JsObject], query: JsObject)


