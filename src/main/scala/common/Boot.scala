package common

import com.typesafe.scalalogging.LazyLogging
import common.Configuration.DbConfig._
import db.{ NodeDbTable, TreeDbTable }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

object Boot extends App with LazyLogging {

  //make abstract interface
  lazy val database = Await.result(initDb, 10 seconds)

  def initDb: Future[DatabaseConfig[JdbcProfile]] = {

    logger.info(s"Opening database for conf '$configPath' @ $jdbcUrl")
    val database = DatabaseConfig.forConfig[JdbcProfile](configPath)
    //db setup and table creation if necessary
    import database.profile.api._

    val created = database.db.run({
      logger.info(s"Setting up schemas and populating tables")
      DBIO.seq(
        (NodeDbTable.nodesTable.schema ++
          TreeDbTable.treeTable.schema).create
      )
    })

    created.map(_ => database)
  }

  logger.info(s"Done initializing db")

}
