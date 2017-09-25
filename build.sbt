name := "tam"
organization := "net.oltiv"
version := "0.0.1"
      
lazy val `tam` = (project in file(".")).enablePlugins(PlayScala,PlayNettyServer,PlayEbean).disablePlugins(PlayAkkaHttpServer)

//resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
resolvers += "Local" at "file://"+Path.userHome.absolutePath+"/.ivy2/local/"
      
scalaVersion := "2.12.3"
ivyScala := ivyScala.value map {_.copy(overrideScalaVersion = true)}
scalacOptions ++= Seq("-feature","-unchecked","-Xlint:unsound-match","-deprecation","-Yno-adapted-args") //"-Ylog-classpath","-Xlog-implicits"
javacOptions in Compile ++= Seq("-Xlint:unchecked","-Xlint:deprecation")
incOptions := incOptions.value.withNameHashing(true)

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "42.1.4",
  "net.oltiv" % "scala-ebean-macros" % "0.3.1",

  //"io.ebean" % "ebean" % "10.4.7",
  //"io.ebean" % "ebean-agent" % "10.4.1",

  "com.typesafe.play" %% "play-mailer" % "6.0.1",

  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.200",

  "com.typesafe.akka" %% "akka-remote" % "2.5.4",
  //"com.typesafe.akka" %% "akka-http" % "10.0.10",

  "com.pauldijou" %% "jwt-play-json" % "0.14.0",

  "com.tokbox" % "opentok-server-sdk" % "3.1.0",
  "com.twilio.sdk" % "twilio" % "7.14.5",

  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"

  //"org.bytedeco" % "javacv-platform" % "1.3.2",
  //"com.ibm.icu" % "icu4j" % "59.1"
)

sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value
TwirlKeys.templateImports in Compile ++= Seq(
  "models._",
  "base.MyConfigImplicit.MyConfig",
  "base.controllers._",
  "base.types._",
  "base.controllers.RequestWrapperForTemplates._"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

PlayKeys.externalizeResources := true
updateOptions := updateOptions.value.withCachedResolution(true)

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

/*
coverageEnabled := false
coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*"
coverageMinimum := 80
coverageFailOnMinimum := false
*/

//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

lazy val build = taskKey[Unit]("Stage synonym")
build := {
  stage.toTask.value
}

      