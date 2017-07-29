package com.github.reactive.notification

import scala.concurrent.{ExecutionContext, Future}


final case class PaymentNotification(email: String, info: String)
final case class NotificationResult(info: String)


trait NotificationService {

  def notifyCustomer(info: PaymentNotification)(implicit ec: ExecutionContext): Future[NotificationResult] = Future {
    NotificationResult("email sent!")
  }

}
