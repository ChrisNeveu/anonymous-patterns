import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

lazy val root = Project(
   id = "root",
   base = file("."),
   aggregate = Seq(core),
   settings = commonSettings ++ Seq(
      publishArtifact := false,
      crossVersion := CrossVersion.binary,
      crossScalaVersions := Seq("2.11.8")
   )
)

lazy val core = Project(
   "core",
   file("core"),
   settings = commonSettings ++ Seq(
      version := "0.0.1-SNAPSHOT",
      scalacOptions in Test += "-Xplugin:" + (packageBin in Compile).value,
      //scalacOptions in Test += "-Yrangepos",
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      libraryDependencies := {
         CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, 10)) =>
               libraryDependencies.value :+ ("org.scalamacros" %% "quasiquotes" % "2.0.1")
            case _ => libraryDependencies.value
         }
      },
      libraryDependencies ++= Seq(
         "org.scala-lang" % "scala-compiler" % scalaVersion.value,
         "org.scalatest" %% "scalatest" % "3.0.0" % "test"
      )
   )
)

lazy val pomStuff = {
  <url>https://github.com/ChrisNeveu/macrame</url>
  <licenses>
    <license>
      <name>BSD 3-Clause</name>
      <url>https://raw.githubusercontent.com/ChrisNeveu/macrame/master/LICENSE</url>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:github.com/ChrisNeveu/macrame.git</connection>
    <developerConnection>scm:git:git@github.com:ChrisNeveu/macrame.git</developerConnection>
    <url>git@github.com:ChrisNeveu/macrame</url>
  </scm>
  <developers>
    <developer>
      <name>Chris Neveu</name>
      <url>chrisneveu.com</url>
    </developer>
  </developers>
}

lazy val commonSettings = Defaults.defaultSettings ++ scalariformSettings ++ Seq(
   organization := "com.chrisneveu",
   scalaVersion := "2.11.8",
   scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-language:postfixOps"
   ),
   useGpg := true,
   pomExtra := pomStuff,
   ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(IndentSpaces, 3)
      .setPreference(SpaceBeforeColon, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
)
