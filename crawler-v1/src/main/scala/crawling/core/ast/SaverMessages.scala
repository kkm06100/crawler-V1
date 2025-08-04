package crawling.core.ast

object SaverMessages {

  sealed trait Guardian
  final case class Save(url: String, actorPath: String, content: String) extends Guardian

  sealed trait Saver
  final case class SaveContent(url:String, content: String) extends Saver
}
