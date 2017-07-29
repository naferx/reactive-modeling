import Common._


lazy val `notification` = (project in file("notification")).
  settings(settings: _*).
  settings(name := "notification")

lazy val `enricher` = (project in file("enricher")).
  settings(settings: _*).
  settings(name := "enricher")

lazy val `payment` = (project in file("payment")).
  dependsOn(`notification`, `enricher`).
  settings(settings: _*).
  settings(name := "payment")

lazy val `root` = (project in file(".")).
  aggregate(`notification`, `enricher`, `payment`)

lazy val settings =
  commonSettings

