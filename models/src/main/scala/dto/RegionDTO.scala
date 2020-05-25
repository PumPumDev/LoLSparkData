package dto

sealed abstract class RegionDTO

case object ru extends RegionDTO

case object kr extends RegionDTO

case object br1 extends RegionDTO

case object oc1 extends RegionDTO

case object jp1 extends RegionDTO

case object na1 extends RegionDTO

case object eun1 extends RegionDTO

case object euw1 extends RegionDTO

case object tr1 extends RegionDTO

case object la1 extends RegionDTO

case object la2 extends RegionDTO

object RegionDTO {
  def getAllRegions: List[RegionDTO] = List(ru, br1, eun1, euw1, jp1, kr, la1, la2, na1, oc1, tr1)
}