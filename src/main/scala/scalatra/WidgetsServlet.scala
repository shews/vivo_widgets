package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.{ElvisOperator,Json,Int,Timer}
import edu.duke.oit.vw.solr._
import java.net.URL
import org.scalatra._
import scalate.ScalateSupport

// use scala collections with java iterators
import scala.collection.JavaConversions._

trait FormatType
object FormatHTML extends FormatType
object FormatJS extends FormatType
object FormatJSON extends FormatType
object FormatJSONP extends FormatType

class WidgetsFilter extends ScalatraFilter
  with ScalateSupport 
  with ScalateTemplateStringify 
  with Timer {

  // GET /people/{collectionName}/5.jsonp?uri={uri}
  get("/api/v0.9/people/:collectionName/:count.:format") {
    renderPeople
  }

  // GET /organizations/{collectionName}/5.jsonp?uri={uri}
  get("/api/v0.9/organizations/:collectionName/:count.:format") {
    renderOrganizations
  }

  // GET /search.json?query=theory*
  get("/search.:format") {
    requestSetup
    val result = VivoSearcher.search(params.getOrElse("query",""), WidgetsConfig.vivoServer)
    format(FormatJSONP) match {
      case FormatJSON => result.toJson
      case FormatHTML => "Not available: html" 
      case FormatJSONP => "vivoSearchResult("+result.toJson+")" //.toJson
      case _ => "NoContent"
    }
  }

  get("/builder") {
    WidgetsConfig.prepareCore
    SolrEntity.getByUri(params("uri")) match {
      case Some(p:Person) => {
        val d = uriParams ++ Map("person" -> p)
        contentType = "text/html"
        templateEngine.layout(TemplateHelpers.tpath("builder/person.jade"), d)
      }
      case Some(o:Organization) => {
        val d = uriParams ++ Map("organization" -> o)
        contentType = "text/html"
        templateEngine.layout(TemplateHelpers.tpath("builder/organization.jade"), d)
      }
      case _ => "NoContent"
    }
  }

  protected def renderPeople = {
    WidgetsConfig.prepareCore
    requestSetup
    Person.find(params("uri"), WidgetsConfig.widgetServer) match {
      case Some(person) => { 
        params.getOrElse('collectionName, "") match {
          case "publications" => renderCollectionData(person.publications)
          case "grants" => renderCollectionData(person.grants)
          case "courses" => renderCollectionData(person.courses)
          case x => "Collection not found: " + x
        }
      }
      case _ => "Not Found"
    }
  }

  protected def renderOrganizations = {
    WidgetsConfig.prepareCore
    requestSetup
    Organization.find(params("uri"), WidgetsConfig.widgetServer) match {
      case Some(organization) => {
        params.getOrElse('collectionName, "") match {
          case "people" => renderCollectionData(organization.people)
          case "grants" => renderCollectionData(organization.grants)
          case x => "Collection not found: " + x
        }
      }
      case _ => "Not Found"
    }
  }

  protected def formatCollection(formatType: FormatType, collectionName: String, collection: List[AnyRef], items: Option[Int], formatting: String, style: String):String  = {
    var modelData = scala.collection.mutable.Map[String,Any]()
    items match {
      case Some(x:Int) => modelData.put(collectionName, collection.slice(0, x))
      case _ => modelData.put(collectionName, collection)
    }
    modelData.put("style", style)
    modelData.put("formatting", formatting)
    modelData.put("searchURI", uri("/search.html"))
    modelData.put("layout", "")

    val template = TemplateHelpers.tpath(collectionName + ".jade")

    timer("WidgetsServlet.formatCollection") {
      renderTemplateString(servletContext, template, modelData.toMap)
    }.asInstanceOf[String]
  }
  
  protected def renderCollectionData(collection: List[AnyRef]) = {
    request("format") match {
      case FormatJSON => Json.toJson(collection)
      case FormatJSONP => "vivoWidgetResult(" + Json.toJson(collection) + ");"
      case FormatHTML => {
        timer("WidgetsServlet.renderCollectionData") {
          formatCollection(FormatHTML, params("collectionName"),
                           collection,
                           Int(params.getOrElse("count", "all")),
                           params.getOrElse("formatting", "detailed"),
                           params.getOrElse("style", "yes"))
        }
      }
      case FormatJS => {
        val output = formatCollection(FormatJS, params("collectionName"),
                                      collection,
                                      Int(params.getOrElse("count", "all")),
                                      params.getOrElse("formatting", "detailed"),
                                      params.getOrElse("style", "yes"))
        val lines = output.split('\n').toList
        val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
        documentWrites.mkString("\n")
      }
      case _ => "not content"
    }
  }
  
  protected def requestSetup = {
    request.put("format", format())
    setContentType(request("format").asInstanceOf[FormatType])
  }
  
  protected def setContentType(formatType:FormatType) = {
    formatType match {
      case FormatJSON => contentType = "application/json"
      case FormatJSONP => contentType = "application/javascript"
      case FormatJS => contentType = "application/javascript"
      case FormatHTML => contentType = "text/html"
      case _ => contentType = "text/unknown"
    }
  }

  protected def format(defaultType:FormatType=FormatJSON):FormatType = {
    params.getOrElse("format", "") match {
      case "json" => FormatJSON
      case "jsonp" => FormatJSONP
      case "html" => FormatHTML
      case "js" => FormatJS
      case _ => defaultType
    }
  }

  protected def uriParams = {
    import edu.duke.oit.vw.utils.ElvisOperator._
    Map(
      "uriPrefix" -> uriPrefix(),
      "contextUri" -> (request.getContextPath() ?: ""),
      "theme" -> WidgetsConfig.theme,
      "version" -> defaultVersion
    )
  }

  protected def defaultVersion = "v0.9"
  
  protected def uri(s:String) = {
    uriPrefix + s
  }
  
  protected def uriPrefix() = {
    import edu.duke.oit.vw.utils.ElvisOperator._
    (request.getContextPath() ?: "") + (request.getServletPath() ?: "")
  }

  notFound {
    filterChain.doFilter(request, response)
  }
  
}
