package domain.crawling.core.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import domain.crawling.core.{Created, Deny, Processing, Scheduled, Status, Terminated}
import domain.crawling.core.ast.CrawlerMessages._

object CrawlingStatusKeeper {

  def apply(): Behavior[StatusKeeper] =
    keepStatus(Created)

  private def keepStatus(status: Status): Behavior[StatusKeeper] =
    Behaviors.receive{ (context, message) => message match {
      case ChangeScheduled() =>
        keepStatus(Scheduled)
      case ChangeProcessing() =>
        keepStatus(Processing)
      case ChangeTerminated() =>
        keepStatus(Terminated)
      case ChangeDeny() =>
        keepStatus(Deny)
      case GetStatus(replyTo) =>
        replyTo ! status
        Behaviors.same
    }}
}
