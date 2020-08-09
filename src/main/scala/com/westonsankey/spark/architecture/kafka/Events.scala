package com.westonsankey.spark.architecture.kafka

import java.util.Date

object Events {

  case class PageViewed(
    sessionId: String,
    url:       String,
    browser:   String,
    userAgent: String
  )

  case class LinkClicked(
    sessionId:  String,
    currentUrl: String,
    linkUrl:    String,
    browser:    String,
    userAgent:  String
  )

  case class EventEnvelopeWrapper(
    eventId:        String,
    eventType:      String,
    eventTimestamp: Date,
    event:          String
  )
}
