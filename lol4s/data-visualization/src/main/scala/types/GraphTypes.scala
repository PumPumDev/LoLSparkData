package types

sealed abstract class GraphTypes{
  def extension: String
}

case object Table extends GraphTypes {
  override def extension: String = ".tab"
}

case object Gii extends GraphTypes {
  override def extension: String = ".graph.ii"
}

case object Gdd extends GraphTypes {
  override def extension: String = ".graph.dd"
}

case object Gid extends GraphTypes {
  override def extension: String = ".graph.id"
}

case object Gsi extends GraphTypes {
  override def extension: String = ".graph.si"
}

case object Gsd extends GraphTypes {
  override def extension: String = ".graph.sd"
}


