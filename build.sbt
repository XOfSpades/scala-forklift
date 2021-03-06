val repoKind = SettingKey[String]("repo-kind",
  "Maven repository kind (\"snapshots\" or \"releases\")")

lazy val slickVersion = "3.1.1"

lazy val coreDependencies = List(
  "org.scala-lang" % "scala-compiler" % "2.11.8",
  "com.typesafe" % "config" % "1.3.0",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
)

lazy val slickDependencies = coreDependencies ++ List(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "io.github.nafg" %% "slick-migration-api" % "0.3.0"
)

lazy val slickDependenciesWithTests = slickDependencies ++ List(
  "org.scalatest" %% "scalatest" % "2.2.5",
  "com.lihaoyi" %% "ammonite-ops" % "0.7.0",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.h2database" % "h2" % "1.4.192",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2",
  "mysql" % "mysql-connector-java" % "5.1.39",
  "org.postgresql" % "postgresql" % "9.4.1209",
  "org.hsqldb" % "hsqldb" % "2.3.4",
  "org.apache.derby" % "derby" % "10.11.1.1"
).map(_ % "test")

lazy val commonSettings = Seq(
  organization := "com.liyaos",
  licenses := Seq("Apache 2.0" -> url("https://github.com/lastland/scala-forklift/blob/master/LICENSE")),
  homepage := Some(url("https://github.com/lastland/scala-forklift")),
  scalaVersion := "2.11.8",
  scalacOptions += "-deprecation",
  scalacOptions += "-feature",
  resolvers += Resolver.bintrayRepo("naftoligug", "maven"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  repoKind <<= (version)(v =>
    if(v.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"),
  publishTo <<= (repoKind){
    case "snapshots" => Some("snapshots" at
        "https://oss.sonatype.org/content/repositories/snapshots")
    case "releases" =>  Some("releases"  at
        "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  },
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  pomExtra := (
    <scm>
      <url>git@github.com:lastland/scala-forklift.git</url>
      <connection>scm:git:git@github.com:lastland/scala-forklift.git</connection>
      </scm>
      <developers>
      <developer>
      <id>lastland</id>
      <name>Yao Li</name>
      </developer>
      </developers>))

lazy val root = Project(
  "scala-forklift", file(".")).settings(
  publishArtifact := false).aggregate(
  coreProject, slickMigrationProject, plainMigrationProject, gitToolProject)

lazy val coreProject = Project(
  "scala-forklift-core", file("core")).settings(
  commonSettings:_*).settings {
  libraryDependencies ++= coreDependencies
}

lazy val slickMigrationProject = Project(
  "scala-forklift-slick", file("migrations/slick")).dependsOn(
  coreProject).settings(commonSettings:_*).settings {
  libraryDependencies ++= slickDependenciesWithTests
}

lazy val plainMigrationProject = Project(
  "scala-forklift-plain", file("migrations/plain")).dependsOn(
  coreProject).settings(commonSettings:_*)

lazy val gitToolProject = Project(
  "scala-forklift-git-tools", file("tools/git")).dependsOn(
  coreProject).settings(commonSettings:_*)
