package com.github.reactive.payment

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.reactive.enricher.{EnrichStage, TransactionEnrich}
import com.github.reactive.notification.{NotificationService, PaymentNotification}
import com.github.reactive.payment.model.Transaction

import scala.concurrent.{ExecutionContext, Future}


final case class Payment(amount: Double)

trait PaymentService extends EnrichStage with NotificationService {

  def processTransaction(transaction: Transaction)(implicit materializer: ActorMaterializer, ec: ExecutionContext): Future[TransactionResult] = {
    Source.single(transaction).
      mapAsync(3)(t => enrich(TransactionEnrich(t.debitCredit))).
      mapAsync(3)(p => notifyCustomer(PaymentNotification("email", "info"))).
      map { elem => TransactionResult("success") }.
      runWith(Sink.head)
  }

}
