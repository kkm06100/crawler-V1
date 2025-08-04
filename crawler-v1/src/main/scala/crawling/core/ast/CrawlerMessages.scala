package crawling.core.ast

import akka.actor.typed.ActorRef

object CrawlerMessages {

  sealed trait Guardian
  final case class Crawl(url: String) extends Guardian
  final case class CrawlingStatus(url: String) extends Guardian

  sealed trait UrlDuplicator
  final case class CheckUrl(url: String, errorHandler: ActorRef[ErrorHandler]) extends UrlDuplicator

  sealed trait DomainThrottler
  final case class CheckDomain(url: String, replyTo: ActorRef[Boolean]) extends DomainThrottler
  final case class DomainCooldownFinished(domain: String) extends DomainThrottler

  sealed trait StatusKeeper
  final case class Scheduled() extends StatusKeeper
  final case class Processing() extends StatusKeeper
  final case class Deny() extends StatusKeeper
  final case class Terminated() extends StatusKeeper
  final case class GetStatus(replyTo: ActorRef[Guardian]) extends StatusKeeper

  sealed trait ErrorHandler // todo ActorContext 로 넘기자
  final case class UrlAlreadyUsed() extends ErrorHandler
  final case class DomainTooManyVisited() extends ErrorHandler
}