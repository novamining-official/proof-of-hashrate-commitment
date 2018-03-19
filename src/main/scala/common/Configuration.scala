package common

object Configuration {

  private lazy val config = com.typesafe.config.ConfigFactory.load()

  object DbConfig {
    val configPath = "database"
    val jdbcUrl = config.getString(s"$configPath.db.url")
  }


}
