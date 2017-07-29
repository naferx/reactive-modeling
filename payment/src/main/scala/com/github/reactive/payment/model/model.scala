package com.github.reactive.payment.model

import java.time.ZonedDateTime

final case class Amount(amount: BigDecimal)

sealed trait TransactionType {
  def id: String
}

object TransactionType {
  def fromString(s: String): TransactionType = s.toLowerCase match {
    case "d" => Debit
    case "c" => Credit
  }
}

case object Debit extends TransactionType {
  override def id: String = "d"
}

case object Credit extends TransactionType {
  override def id: String = "c"
}


final case class Transaction(id: String, accountNo: String,
                       debitCredit: String, amount: Amount, date: ZonedDateTime = ZonedDateTime.now())