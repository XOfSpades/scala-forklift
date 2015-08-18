package com.liyaos.forklift.slick

import com.typesafe.config._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable
import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.core.MigrationManager

trait SlickMigrationManager
    extends MigrationManager[Int, slick.dbio.DBIO[Unit]] {
  val config = SlickMigrationsConfig.config

  import config.driver.api._

  class MigrationsTable(tag: Tag) extends Table[Int](tag, "__migrations__") {
    def id = column[Int]("id", O.PrimaryKey)
    def * = id
  }

  class DummyTable(tag: Tag, name: String) extends Table[Int](tag, name) {
    def id = column[Int]("id")
    def * = id
  }

  type SlickMigration = Migration[Int, DBIO[Unit]]

  val db = config.db

  lazy val migrationsTable = TableQuery[MigrationsTable]
  override def init = {
    val f = db.run(migrationsTable.schema.create)
    Await.result(f, Duration.Inf)
  }
  override def alreadyAppliedIds = {
    val f = db.run(migrationsTable.map(_.id).result)
    Await.result(f, Duration.Inf)
  }
  def latest = alreadyAppliedIds.last

  override protected def up(migrations: Iterator[SlickMigration]) = {
    val ups = DBIO.sequence(migrations flatMap { m =>
      List(m.up, migrationsTable += m.id)
    })
    val f = db.run(ups)
    Await.result(f, Duration.Inf)
  }

  override def reset = {
    val drop = MTable.getTables.flatMap { s =>
      DBIO.sequence(s map { t =>
        TableQuery(new DummyTable(_, t.name.name)).schema.drop
      })
    }
    val f = db.run(drop)
    Await.result(f, Duration.Inf)
  }
}
