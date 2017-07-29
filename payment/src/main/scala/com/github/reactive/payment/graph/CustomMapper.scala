package com.github.reactive.payment.graph

import akka.stream.{Outlet, Inlet, Attributes, FlowShape}
import akka.stream.stage.{InHandler, OutHandler, GraphStageLogic, GraphStage}


final class CustomMapper[In, Out](f: In => Out) extends GraphStage[FlowShape[In, Out]]{

  val in = Inlet[In]("Mapper.in")
  val out = Outlet[Out]("Mapper.out")

  override def shape: FlowShape[In, Out] = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with OutHandler with InHandler {

      //state here

      override def onPush(): Unit = {
        val elem: In = grab(in)
        push(out, f(elem))
      }

      override def onPull(): Unit = {
        pull(in)
      }

      setHandlers(in, out, this)
    }

}