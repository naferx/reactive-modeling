package com.github.reactive.enricher


import scala.concurrent.{ExecutionContext, Future}

final case class TransactionEnrich(`type`: String)

final case class EnrichMessageResult(txnType: Int)


trait EnrichStage {

 /* implicit class EnrichStageOps[In, Out](f: Flow[In, EnrichMessage, NotUsed]) {
    def enrich(parallelism: Int = 0) = Flow[EnrichMessage].mapAsync(parallelism)(x => enrich2(x))
  }
*/
  def enrich(transaction: TransactionEnrich)(implicit ec: ExecutionContext) = Future {
    val newType = transaction.`type` match {
      case "c" => 111
      case "d" => 222
      case _ => 0 //
    }
    EnrichMessageResult(newType)
  }

}

