package main

import java.nio.file.{ Files, Paths }

import db.TreeStore
import org.scalatest.{ BeforeAndAfter, Suite }
import proof.domain.Account

import scala.io.Source
import scala.util.Random
import scala.collection.JavaConverters._

object Helpers {

  trait CleanDBStore extends BeforeAndAfter { testSuite: Suite =>
    //Clean the test store dir every time we're about to run the test
    before {
      Files.list(TreeStore.storeDir).iterator.asScala.map { file =>
        Files.deleteIfExists(file)
      }
    }

    after {
      Files.list(TreeStore.storeDir).iterator.asScala.map { file =>
        Files.deleteIfExists(file)
      }
    }

  }

  def resourceAsString(fileName: String) = Source.fromURL(
    getClass.getResource(s"/$fileName")
  ).mkString

  def writeToFile(data: String, fileName: String) = Files.write(
    Paths.get(s"src/test/resources/$fileName"),
    data.getBytes
  )

  def passingTestMock = resourceAsString("mocks/mock_data.json")
  def accountsTestMock = resourceAsString("mocks/accounts.json")

  lazy val randomAccounts: Stream[Account] = Account(
    user = Random.alphanumeric.take(6).mkString,
    balance = Random.nextInt,
    nonce = Random.alphanumeric.take(4).mkString
  ) #:: randomAccounts

}
