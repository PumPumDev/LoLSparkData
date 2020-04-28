package usefull

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
}