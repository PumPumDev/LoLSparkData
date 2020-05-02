package dto

sealed abstract class RegionDTO

case object RU extends RegionDTO {
  override def toString: String = "ru"
}

case object KR extends RegionDTO {
  override def toString: String = "kr"
}

case object BR extends RegionDTO {
  override def toString: String = "br1"
}

case object OC extends RegionDTO {
  override def toString: String = "oc1"
}

case object JP extends RegionDTO {
  override def toString: String = "jp1"
}

case object NA extends RegionDTO {
  override def toString: String = "na1"
}

case object EUN extends RegionDTO {
  override def toString: String = "eun1"
}

case object EUW extends RegionDTO {
  override def toString: String = "euw1"
}

case object TR extends RegionDTO {
  override def toString: String = "tr1"
}

case object LA1 extends RegionDTO {
  override def toString: String = "la1"
}

case object LA2 extends RegionDTO {
  override def toString: String = "la2"
}

object RegionDTO {
  def getAllRegions: List[RegionDTO] = List(RU, BR, EUN, EUW, JP, KR, LA1, LA2, NA, OC, TR)
}