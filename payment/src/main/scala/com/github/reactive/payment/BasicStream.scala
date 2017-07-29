package com.github.reactive.payment

import akka.actor.{ActorRef, ActorSystem}
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.{Done, NotUsed}
import com.github.reactive.notification.NotificationStage

import scala.collection.immutable.Iterable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import akka.event.Logging
import com.github.reactive.payment.graph.{CustomFilter, CustomMapper}




object BasicStream extends App {

  basic()
  //  custom()
  // zip()
  // graph()
 // pipelining()
 //  extendingOps()



  def zip() = {
    println("Zip pipeline with built-in Flow............")
    println(Thread.currentThread().getName)

    val system = ActorSystem("BasicSystem")
    implicit val materializer = ActorMaterializer.create(system)

    val index: Source[Int, NotUsed] = Source( 1 to 5) //publisher

    val source: Source[String, NotUsed] = Source(List("ab",  "xdc", "ptl")) //publisher
    //.log("before-map")
    //.withAttributes(Attributes.logLevels(onElement = Logging.WarningLevel))

    //Flow
    val zip = Flow[String].zip(index)
    //val zip Flow[Int, Int, NotUsed] = Flow[Int].zip(x => x > 3)

    val sink = Sink.foreach(println)

    val graph: RunnableGraph[NotUsed] = source.via(zip).to(sink) //blueprint

    graph.run()(materializer)
  }

  def graph() = {
    val system = ActorSystem("BasicSystem")
    implicit val materializer = ActorMaterializer.create(system)
    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val in = Source(1 to 10)
      val out = Sink.foreach(println)

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
                  bcast ~> f4 ~> merge
      ClosedShape
    })

    g.run()
  }


  def basic() = {
    println("Basic pipeline with built-in Flow............")
    println(Thread.currentThread().getName)

    val system = ActorSystem("BasicSystem")
    val materializer = ActorMaterializer.create(system)

    val source: Source[Int, String] = Source(1 to 10) //publisher
      .log("before-map")
      .withAttributes(Attributes.logLevels(onElement = Logging.WarningLevel))
    .mapMaterializedValue(_ => "Hola")

    //Flow
    val filter: Flow[Int, Int, NotUsed] = Flow[Int].filter(x => x > 3)

    val map: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 1)

    val sink = Sink.foreach(println)

    val graph: RunnableGraph[String] =
      source.
      via(filter).
      via(map).
      to(sink)

    val result: String = graph.run()(materializer)
    println("MATERIALIZED VALUE " + result)
  }


  def custom() = {
    println("Custom Flow............")

    val system = ActorSystem("CustomSystem")
    implicit val materializer = ActorMaterializer.create(system)

    val source: Source[Int, NotUsed] = Source(List(2, 1, 4, 6, 3, 17, 18, 20))

    // Flow
    val filter = new CustomFilter[Int]((x: Int) => x % 2 != 0) // selecciona solo los impares
    val transformation = new CustomMapper[Int, String]((x: Int) => s"elem: $x")

    val sink = Sink.foreach(println)

    source.
      via(filter).async.
      log("on-filter").
      withAttributes(Attributes.logLevels(onElement = Logging.WarningLevel)).
      via(transformation).async.
      log("on-map").
      withAttributes(Attributes.logLevels(onElement = Logging.WarningLevel)).
      runWith(sink).
      onComplete(_ => system.terminate)

    // Reusable - Immutability
    source.
      map(identity[Int]).
      runWith(sink).
      onComplete(_ => system.terminate)

  }


  def pipelining() = {
    println("Pipelining & Parallelism............")

    implicit val system = ActorSystem("CustomSystem")
    implicit val materializer = ActorMaterializer.create(system)
    import system.dispatcher

    println(Thread.currentThread().getName)
  }


  def extendingOps() = {

    implicit val system = ActorSystem("BasicStream")
    implicit val materializer = ActorMaterializer()


    implicit class FlowExtensions[Out, Mat](s: Source[Out, Mat]) {
      def debit[T](f: Out => T): Source[T, Mat] = s.map(f)
    }

    implicit class SourceExtensions[Out, Mat](s: Source[Out, Mat]) {
      def validateFunds(f: Out => Boolean): Source[Out, Mat] = s.filter(f)
    }

    val payments: Source[Int, NotUsed] = Source[Int](1 to 5)
    val sink: Sink[Double, Future[Done]] = Sink.foreach[Double](println)

    val businessProcess =
            payments.
                validateFunds( _ > 3).
                debit( _ * 0.9).
                toMat(sink)(Keep.right)

      businessProcess.run().onComplete {
        case Success(_) =>
          system.log.info("Execution completed!")
          system.terminate()
        case Failure(f) =>
          system.log.error("Error {}", f)
          system.terminate()
      }

    // sys.addShutdownHook(system.terminate())
  }



}
