package domain.crawling.core.status

sealed trait Status
case object Created extends Status
case object Scheduled extends Status
case object Processing extends Status
case object Deny extends Status
case object Terminated extends Status
