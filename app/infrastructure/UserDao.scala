package infrastructure

import model.User
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

sealed trait UserTable {

  protected val driver: JdbcProfile

  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def mail = column[String]("mail")

    override def * : ProvenShape[User] = (id.?, name, mail) <>((User.apply _).tupled, User.unapply)
  }

}

class UserDao(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends UserTable {

  import dbConfig.driver.api._

  private val users = TableQuery[Users]

  protected val driver = dbConfig.driver

  def findAll(): Future[Seq[User]] =
    dbConfig.db.run(users.result).map(_.toSeq)

  def findById(id: Long): Future[Option[User]] =
    dbConfig.db.run(users.filter(_.id === id).result.headOption)

  def insert(user: User): Future[Int] =
    dbConfig.db.run(users += user)

  def update(user: User): Future[Int] =
    dbConfig.db.run(users.filter(_.id === user.id).update(user))

  def delete(id: Long): Future[Int] =
    dbConfig.db.run(users.filter(_.id === id).delete)

}
