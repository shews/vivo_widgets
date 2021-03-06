package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class PersonReference(uri:String,
                           vivoType:String,
                           label:String,
                           title:String,
                           attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object PersonReference extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              useCache: Boolean = false, templatePath: String="sparql/organization/people.ssp") = {
    val personReferenceData = vivo.selectFromTemplate(templatePath, uriContext, useCache)
    personReferenceData.map(build(_)).asInstanceOf[List[PersonReference]]

  }

  def build(person:Map[Symbol,String]) = {
    new PersonReference(uri         = person('person).stripBrackets(),
                        vivoType    = person('type).stripBrackets(),
                        label       = person('label),
                        title       = person('title),
                        attributes  = parseAttributes(person, List('person, 'type, 'label, 'title)))
  }

}

