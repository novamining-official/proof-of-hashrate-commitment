package common

object Config {

  private lazy val config = com.typesafe.config.ConfigFactory.load()

  val dbDirectory = config.getString("db.directory")
  val apiHost = config.getString("api.host")
  val apiPort = config.getInt("api.port")

}
