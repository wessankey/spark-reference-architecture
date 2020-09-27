package com.westonsankey.spark.architecture

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.{col, from_json}
import com.redis._
import com.westonsankey.spark.architecture.Model.S3Destination

object Main extends App {

  // Create Spark session and set required Delta configurations
  val spark: SparkSession = SparkSession.builder()
    .appName("streaming-demo")
    .master("local[*]")
    .config("spark.delta.logStore.class",
      "org.apache.spark.sql.delta.storage.S3SingleDriverLogStore")
    .getOrCreate()

  // Set AWS access keys required for writing to S3
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", sys.env("AWS_ACCESS_KEY"))
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", sys.env("AWS_SECRET_KEY"))

  val redis = new RedisClient(
    sys.env.getOrElse("REDIS_HOST", "localhost"),
    sys.env.getOrElse("REDIS_PORT", "6379").toInt
  )

  val dest = S3Destination(
    sys.env("S3_BUCKET"),
    sys.env.getOrElse("S3_PREFIX", "delta-demo")
  )

  val eventStreamWriter = new EventStreamWriter(redis, dest)

  // Read raw events from Kafka
  val rawEvents = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", sys.env("KAFKA_BOOTSTRAP_SERVERS"))
    .option("subscribe", sys.env("KAFKA_TOPIC"))
    .option("startingOffsets", "latest")
    .load()

  rawEvents
    .select(col("topic"), expr("cast(value as string) as value"))
    .withColumn("parsed",
      from_json(col("value"), Model.envelopeWrapperSchema))
    .writeStream
    .foreachBatch((df: DataFrame, id: Long) => eventStreamWriter.filter_and_output(df, id))
    .start
    .awaitTermination

}
