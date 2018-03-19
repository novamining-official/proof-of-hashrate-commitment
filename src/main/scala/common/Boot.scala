package common

import common.Configuration.DbConfig.configPath
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object Boot extends App {

  val database = {
    // logger.info(s"Opening database for conf '$configPath' @ $jdbcUrl")

    //      if (webUI) {
    //        logger.info(s"Creating web ui @ localhost:8888")
    //        org.h2.tools.Server.createWebServer("-webAllowOthers", "-webPort", "8888").start()
    //      }

    DatabaseConfig.forConfig[JdbcProfile](configPath)
  }

}
