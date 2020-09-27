name := "spark-reference-architecture"

version := "0.1"

scalaVersion := "2.12.10"

val akkaVersion = "2.6.5"
val sparkVersion = "3.0.0"
val deltaVersion = "0.7.0"
val log4jVersion = "2.4.1"


resolvers ++= Seq(
  "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven",
  "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases",
  "MavenRepository" at "https://mvnrepository.com"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-avro" % sparkVersion,

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.4",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,

  "net.debasishg" %% "redisclient" % "3.30",

  "io.delta" %% "delta-core" % deltaVersion,
  "org.apache.hadoop" % "hadoop-aws" % "2.7.7",

  "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion
)
