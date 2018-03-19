import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "araspitzu",
      scalaVersion    := "2.12.4",
      version         := "0.0.1",
      mainClass in Compile := Some("Boot"),
      ScalariformKeys.preferences := scalariformPref.value
    )),
    name := "proof_of_hashrate_commitment",
    libraryDependencies ++= Seq(
      //JSON
      "org.json4s" %% "json4s-native" % "3.5.3",
      "org.json4s" %% "json4s-jackson" % "3.5.3",

      //TEST FRAMEWORK
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",

      //DB
      "com.typesafe.slick" %% "slick" % "3.2.+",
      "com.h2database" % "h2" % "1.4.+",
      "com.zaxxer" % "HikariCP" % "2.5.+" % "test",

      //LOGGING FRAMEWORK
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.+"
    )
  )

enablePlugins(JavaAppPackaging)

lazy val scalariformPref = Def.setting {
  ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(CompactStringConcatenation, true)
}
