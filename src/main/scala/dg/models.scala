package dg

object models {
  type UID = String 
  type Predicate = String 
  type Predicates = List[String]
  final case class DgError(reason: String)
}
