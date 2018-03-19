package db

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import common.Configuration.DbConfig._

trait DatabaseManager {

  //  val database: DatabaseImpl
  //
  //  class DatabaseImpl {
  //
  //    val database = {
  //     // logger.info(s"Opening database for conf '$configPath' @ $jdbcUrl")
  //
  ////      if (webUI) {
  ////        logger.info(s"Creating web ui @ localhost:8888")
  ////        org.h2.tools.Server.createWebServer("-webAllowOthers", "-webPort", "8888").start()
  ////      }
  //
  //      DatabaseConfig.forConfig[JdbcProfile](configPath)
  //    }
  //
  //    def db = database.db
  //
  //  }

}
