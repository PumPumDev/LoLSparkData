package usefull

import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

sealed abstract class Region

case object RU extends Region {
  override def toString: String = "ru"
}

case object KR extends Region {
  override def toString: String = "kr"
}

case object BR extends Region {
  override def toString: String = "br1"
}

case object OC extends Region {
  override def toString: String = "oc1"
}

case object JP extends Region {
  override def toString: String = "jp1"
}

case object NA extends Region {
  override def toString: String = "na1"
}

case object EUN extends Region {
  override def toString: String = "eun1"
}

case object EUW extends Region {
  override def toString: String = "euw1"
}

case object TR extends Region {
  override def toString: String = "tr1"
}

case object LA1 extends Region {
  override def toString: String = "la1"
}

case object LA2 extends Region {
  override def toString: String = "la2"
}

object Region {
  def getAllRegions: List[Region] = List(RU, BR, EUN, EUW, JP, KR, LA1, LA2, NA, OC, TR)

  implicit object RegionFormat extends RootJsonFormat[Region] with DefaultJsonProtocol {
    override def write(obj: Region): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Region = json match {
      case JsString(value) => value match {
        case "ru" => RU
        case "br1" => BR
        case "eun1" => EUN
        case "euw1" => EUW
        case "jp1" => JP
        case "kr" => KR
        case "la1" => LA1
        case "la2" => LA2
        case "na1" => NA
        case "oc1" => OC
        case "tr1" => TR
      }
    }
  }

}