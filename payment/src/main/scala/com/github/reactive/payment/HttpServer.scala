package com.github.reactive.payment

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directive, HttpApp, Route}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.github.reactive.payment.model.{Amount, Transaction}

import scala.util.{Failure, Success}


final case class TransactionRequest(account: String, `type`: String, amount: Double)


object HttpServer extends HttpApp with PaymentService with JsonProtocol with App {

  implicit val system: ActorSystem =  ActorSystem("HTTPServer")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import akka.pattern.ask
  import system.dispatcher
  import scala.concurrent.duration._
  implicit val askTimeout: Timeout = 3.seconds

  val loggingRequest: Directive[Unit] =
    extractRequestContext.flatMap { ctx =>
      extractClientIP.flatMap { client =>
        mapRequest { request =>
          ctx.log.info(
            s"${ctx.request.method.name} ${ctx.request.uri.path} ${client.toOption.map(_.getHostAddress).getOrElse("unknown")}"
          )
          request
        }
      }
    }

  override protected def routes: Route = loggingRequest {
    path("clients") {
      post {
        entity(as[TransactionRequest]) { transactionRequest =>
          val transaction = toPayment(transactionRequest)
          onComplete(processTransaction(transaction).mapTo[TransactionResult]) {
            case Success(r) => complete(r)
            case Failure(error) =>
              system.log.error("" + error)
              complete("error")
          }
        }
      }
    }
  }

  private def toPayment(t: TransactionRequest): Transaction = {
    Transaction(
      UUID.randomUUID().toString,
      t.account,
      t.`type`,
      Amount(t.amount)
    )
  }

  override protected def postHttpBinding(binding: Http.ServerBinding): Unit = {
    println(s"Server listening on ${binding.localAddress.getAddress.getHostAddress}: ${binding.localAddress.getPort}")
  }

  HttpServer.startServer("localhost", 8080)
}