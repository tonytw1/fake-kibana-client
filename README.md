## Not a fake Kibana client

Nothing to see here; move along please.

Provides a quick and dirty KibanaSearch trait which can be used to programmatically extract data from the Kibana logstash elasticsearch msearch endpoint.

```
val ws = AhcWSClient()
val kibanaClient = new KibanaClient(ws, kibanaUrl, username, password)

val query: Seq[(String, String)] = Seq(
  ("a-field", "foo"),
  ("another-field", "bah")
)

val fieldsToShow = Seq("some-field", "some-other-field")

val rows: Seq[Seq[String]] = executeSearch(query, fieldsToShow, 10)
```
