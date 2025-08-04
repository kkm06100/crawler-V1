package domain.crawling.core

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import domain.crawling.core.ast.CrawlerMessages.{ChangeDeny, ChangeProcessing, ChangeScheduled, ChangeTerminated, GetStatus, StatusKeeper}
import domain.crawling.core.status.{Created, Deny, Processing, Scheduled, Status, Terminated}

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
