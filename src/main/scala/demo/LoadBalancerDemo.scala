package demo

import messages.Messages._
import backend.Calculator
import frontend.FrontEnd

object LoadBalancerDemo extends App {
  FrontEnd.create

//  Calculator.create("adder")
//
//  Calculator.create("substractor")
//
//  Calculator.create("multiplier")
//
//  Calculator.create("divider")
//
//  Thread.sleep(2000)
//
//  val router = FrontEnd.getRouter
//
//  router ! Add(10,3)
//  router ! Mul(3,7)
//  router ! Div(8,2)
//  router ! Sub(45, 3)
//  router ! Div(8,0)

  Thread.sleep(20000)
}