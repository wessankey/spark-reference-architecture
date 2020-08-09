package com.westonsankey.spark.architecture

import org.apache.spark.sql.types.{StringType, StructField, StructType}

object Model {

  val envelopeWrapperSchema: StructType = StructType(
    Array(
      StructField("eventType", StringType),
      StructField("eventTimestamp", StringType),
      StructField("event", StringType)
    )
  )

  case class S3Destination(
    bucket: String,
    prefix: String
  )

}
