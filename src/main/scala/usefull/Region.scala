package usefull

import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

case class Region(reg: String)

object Region {

  val RU = Region("ru")
  val KR = Region("kr")
  val BR = Region("br1")
  val OC = Region("oc1")
  val JP = Region("jp1")
  val NA = Region("na1")
  val EUN = Region("eun1")
  val EUW = Region("euw1")
  val TR = Region("tr1")
  val LA1 = Region("la1")
  val LA2 = Region("la2")

  def getAllRegions: List[Region] = List(Region.RU, Region.BR, Region.EUN, Region.EUW, Region.JP, Region.KR, Region.LA1, Region.LA2, Region.NA, Region.OC, Region.TR)

  implicit object RegionFormat extends RootJsonFormat[Region] with DefaultJsonProtocol {
    override def write(obj: Region): JsValue = JsString(obj.reg.toString)

    override def read(json: JsValue): Region = json match {
      case JsString(value) => Region(value)
    }
  }

}