package com.github.reactive.payment.graph



import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}

final class CustomFilter[A](f: A => Boolean) extends GraphStage[FlowShape[A, A]]{

  val in = Inlet[A]("Filter.in")
  val out = Outlet[A]("Filter.out")

  override def shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with OutHandler with InHandler {

      //state here
      var previousFiltered: Boolean = false

      override def onPush(): Unit = {
        val elem = grab(in)
        if (previousFiltered) { // no filtra si el anterior fue filtrado
          previousFiltered = false
          push(out, elem)
        } else if (f(elem)) { // si cumple el predicado
          previousFiltered = false
          push(out, elem)
        } else {
          previousFiltered = true
          pull(in)
        }
      }

      override def onPull(): Unit = {
        pull(in)
      }

      setHandlers(in, out, this)
    }
}
