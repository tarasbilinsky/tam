logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "4.0.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.9.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")