package com.westonsankey.spark.architecture.kafka

import java.util.{Date, Random}

import com.westonsankey.spark.architecture.kafka.Events.{EventEnvelopeWrapper, LinkClicked, PageViewed}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write

object RandomEventGenerator {

  implicit val formats: DefaultFormats.type = DefaultFormats
  val rand = new Random()

  val eventTypes = List(
    "page_viewed.v1",
    "link_clicked.v1"
  )

  val urls = List(
    "https://google.com",
    "https://apple.com",
    "https://amazon.com"
  )

  val browsers = List(
    "Chrome",
    "Firefox",
    "Safari"
  )

  /**
   * Create a random page view event.
   *
   * @return Randomly generated page view event
   */
  def createPageViewEvent(): EventEnvelopeWrapper = {
    val sessionId = java.util.UUID.randomUUID().toString
    val url = urls(rand.nextInt(urls.length))
    val browser = browsers(rand.nextInt(browsers.length))
    val event = PageViewed(sessionId, url, browser, "")

    val timestamp = new Date()
    val eventId = java.util.UUID.randomUUID().toString
    EventEnvelopeWrapper(eventId, "page_viewed.v1", timestamp, write(event))
  }

  /**
   * Create a random link clicked event.
   *
   * @return Randomly generated link clicked event
   */
  def createLinkClickedEvent(): EventEnvelopeWrapper = {
    val sessionId = java.util.UUID.randomUUID().toString
    val browser = browsers(rand.nextInt(browsers.length))
    val event = LinkClicked(sessionId, "", "", browser, "")

    val timestamp = new Date()
    val eventId = java.util.UUID.randomUUID().toString
    EventEnvelopeWrapper(eventId, "link_clicked.v1", timestamp, write(event))
  }

  /**
   * Generate the specified number of random events.
   *
   * @param numEvents number of random events to generate
   * @return          list of random events in JSON format
   */
  def generateRandomEvents(numEvents: Int): List[String] = {
    val events = for (_ <- 1 to 50) yield {
      rand.nextInt(1) match {
        case 0 => write(createLinkClickedEvent())
        case 1 => write(createPageViewEvent())
      }
    }

    events.toList
  }

}
