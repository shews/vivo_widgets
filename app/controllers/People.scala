package controllers

import play._
import play.mvc._
import play.templates._

import models.{SolrConnection,VivoConnection,VivoWidgetJsonResult}
import edu.duke.oit.vw.solr._

object People extends Controller {

  def publicationsData(vivoId: String, items: Int) = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => { 
        val vwr = new VivoWidgetJsonResult(person.publications)
        request.format match {
          case "json" => Json(vwr.json.toString)
          case "jsonp" => Json(vwr.jsonp)
          case _ => NoContent
        }
      }
      case _ => NotFound
    }

  }

  def publications(vivoId: String, items: Int, formatting: String = "detailed", style: String = "yes") = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => {
        val publications = person.publications.sortBy{p => p.getOrElse("year","")}.reverse
        val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
        if (items > 0) {
          modelData.put("publications",publications.slice(0,items))
        } else {
          modelData.put("publications",publications)
        }
        modelData.put("style",style)
        modelData.put("formatting",formatting)
        val htmlString = TemplateLoader.load("People/publications.html").render(modelData)
        request.format.toString match {
          case "js" => 
            val lines = htmlString.split('\n').toList
            val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
            val documentWritesString = documentWrites.mkString("\n")
            Json(documentWritesString)
          case "html" => Html(htmlString)
          case _ => NoContent
        }
      }
      case _ => "Not Found"
    }

  }

  def grantsData(vivoId: String, items: Int) = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => { 
        val vwr = new VivoWidgetJsonResult(person.grants)
        request.format match {
          case "json" => Json(vwr.json.toString)
          case "jsonp" => Json(vwr.jsonp)
          case _ => NoContent
        }
      }
      case _ => NotFound
    }

  }

  def grants(vivoId: String, items: Int, formatting: String = "detailed", style: String = "yes") = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => {
        val grants = person.grants
        val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
        if (items > 0) {
          modelData.put("grants",grants.slice(0,items))
        } else {
          modelData.put("grants",grants)
        }
        modelData.put("style",style)
        modelData.put("formatting",formatting)
        val htmlString = TemplateLoader.load("People/grants.html").render(modelData)
        request.format.toString match {
          case "js" => 
            val lines = htmlString.split('\n').toList
            val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
            val documentWritesString = documentWrites.mkString("\n")
            Json(documentWritesString)
          case "html" => Html(htmlString)
          case _ => NoContent
        }
      }
      case _ => "Not Found"
    }
  }

  def coursesData(vivoId: String, items: Int) = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => { 
        val vwr = new VivoWidgetJsonResult(person.courses)
        request.format match {
          case "json" => Json(vwr.json.toString)
          case "jsonp" => Json(vwr.jsonp)
          case _ => NoContent
        }
      }
      case _ => NotFound
    }

  }

  def courses(vivoId: String, items: Int, formatting: String = "detailed", style: String = "yes") = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => {
        val courses = person.courses
        val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
        if (items > 0) {
          modelData.put("courses",courses.slice(0,items))
        } else {
          modelData.put("courses",courses)
        }
        modelData.put("style",style)
        modelData.put("formatting",formatting)
        val htmlString = TemplateLoader.load("People/courses.html").render(modelData)
        request.format.toString match {
          case "js" => 
            val lines = htmlString.split('\n').toList
            val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
            val documentWritesString = documentWrites.mkString("\n")
            Json(documentWritesString)
          case "html" => Html(htmlString)
          case _ => NoContent
        }
      }
      case _ => "Not Found"
    }
  }
}


