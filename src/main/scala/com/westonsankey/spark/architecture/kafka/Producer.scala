package com.westonsankey.spark.architecture.kafka

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}


object Producer extends App {

  /**
   * Create a Kafka producer
   *
   * @return Kafka producer
   */
  def createProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sys.env("KAFKA_BOOTSTRAP_SERVERS"))
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "event-producer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      "org.apache.kafka.common.serialization.StringSerializer")

    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](props)
    producer
  }

  /**
   * Convert an object to a byte array.
   *
   * @param data object to convert
   * @return     byte array representation of object
   */
  implicit def convertToByteArray(data: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(data)
    oos.close()
    stream.toByteArray
  }

  val events = RandomEventGenerator.generateRandomEvents(50)
  val producer = createProducer()
  val topic = sys.env("KAFKA_TOPIC")

  events.foreach { e =>
    val producerRecord: ProducerRecord[String, String] =
      new ProducerRecord[String, String](topic, e)

    producer.send(producerRecord)
  }

  import java.util.concurrent.TimeUnit

  try TimeUnit.SECONDS.sleep(1)
  catch {
    case ex: InterruptedException =>
      ex.printStackTrace()
  }

}
