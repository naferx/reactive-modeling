package com.github.reactive.payment

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


final case class TransactionResult(message: String)


trait JsonProtocol extends SprayJsonSupport with DefaultJsonProtocol{

  implicit val transactionResultFormat = jsonFormat1(TransactionResult)
  implicit val transactionRequestFormat = jsonFormat3(TransactionRequest)

}
