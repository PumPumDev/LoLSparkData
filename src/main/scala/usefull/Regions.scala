package usefull

object Regions extends Enumeration {
  type Region = Value

  val RU: Regions.Value = Value("ru")
  val KR: Regions.Value = Value("kr")
  val BR: Regions.Value = Value("br1")
  val OC: Regions.Value = Value("oc1")
  val JP: Regions.Value = Value("jp1")
  val NA: Regions.Value = Value("na1")
  val EUN: Regions.Value = Value("eun1")
  val EUW: Regions.Value = Value("euw1")
  val TR: Regions.Value = Value("tr1")
  val LA1: Regions.Value = Value("la1")
  val LA2: Regions.Value = Value("la2")

  def getAllRegions: Set[String] = Regions.values.map(_.toString)
}