package treatment

sealed abstract class GraphsTypes {
  def extension : String
}

case object Int_Int extends GraphsTypes {
  override def extension: String = ".ii"
}

case object Double_Double extends GraphsTypes {
  override def extension: String = ".dd"
}

case object Int_Double extends GraphsTypes {
  override def extension: String = ".id"
}

case object String_Int extends GraphsTypes {
  override def extension: String = ".si"
}

case object String_Double extends GraphsTypes {
  override def extension: String = ".sd"
}

case object Int_Long extends GraphsTypes {
  override def extension: String = ".ii"
}
