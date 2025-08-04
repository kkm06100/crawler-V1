package domain.crawling.core.ast

import akka.actor.typed.ActorRef
import domain.crawling.core.status.Status

object CrawlerMessages {

  sealed trait Guardian
  final case class Crawl(url: String) extends Guardian
  final case class CrawlingStatus(url: String) extends Guardian

  sealed trait UrlDuplicator
  final case class CheckUrl(url: String, errorHandler: ActorRef[Boolean]) extends UrlDuplicator

  sealed trait DomainThrottler
  final case class CheckDomain(url: String, replyTo: ActorRef[Boolean]) extends DomainThrottler
  final case class DomainCooldownFinished(domain: String) extends DomainThrottler

  sealed trait StatusKeeper
  final case class ChangeScheduled() extends StatusKeeper
  final case class ChangeProcessing() extends StatusKeeper
  final case class ChangeDeny() extends StatusKeeper
  final case class ChangeTerminated() extends StatusKeeper
  final case class GetStatus(replyTo: ActorRef[Status]) extends StatusKeeper
}