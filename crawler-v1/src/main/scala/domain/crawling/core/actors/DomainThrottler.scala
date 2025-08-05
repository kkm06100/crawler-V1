package domain.crawling.core.actors

import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior}
import domain.crawling.core.ast.CrawlerMessages.{CheckDomain, DomainCooldownFinished, DomainThrottler}

import java.net.URI
import scala.collection.immutable.Queue
import scala.concurrent.duration.DurationInt

object DomainThrottler {

  def apply(): Behavior[DomainThrottler] =
    Behaviors.withTimers { timers =>
      throttling(Map.empty, Set.empty)(timers)
    }

  private def throttling(
                          queueMap: Map[String, Queue[ActorRef[Boolean]]],
                          cooldowns: Set[String]
                        )(timers: TimerScheduler[DomainThrottler]): Behavior[DomainThrottler] = {
    Behaviors.receive { (context, message) =>
      message match {
        case CheckDomain(url, replyTo) =>

          extractDomain(url) match {
            case Some(domain) =>
              if (cooldowns.contains(domain)) {
                val updatedQueue = queueMap.getOrElse(domain, Queue.empty).enqueue(replyTo)
                throttling(queueMap.updated(domain, updatedQueue), cooldowns)(timers)
              } else {
                replyTo ! true
                timers.startSingleTimer(domain, DomainCooldownFinished(domain), 1.second)
                throttling(queueMap, cooldowns + domain)(timers)
              }
            case None =>
              context.log.info(s"Invalid URL: $url")
              replyTo ! false
              Behaviors.same
          }

        case DomainCooldownFinished(domain) =>
          queueMap.get(domain) match {
            case Some(queue) if queue.nonEmpty =>
              val (nextReplyTo, restQueue) = queue.dequeue
              nextReplyTo ! true
              timers.startSingleTimer(DomainCooldownFinished(domain), DomainCooldownFinished(domain), 1.second)
              val newQueueMap = if (restQueue.isEmpty) queueMap - domain else queueMap.updated(domain, restQueue)
              throttling(newQueueMap, cooldowns)(timers)
            case _ =>
              throttling(queueMap - domain, cooldowns - domain)(timers)
          }
      }
    }
  }

  private def extractDomain(url: String): Option[String] = {
    try {
      val uri = new URI(url)
      Option(uri.getHost).map(_.replace("www", ""))
    } catch {
      case _: Exception => None
    }
  }
}
