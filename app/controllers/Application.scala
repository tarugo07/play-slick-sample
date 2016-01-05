package controllers

import infrastructure.UserDao
import model.User
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import slick.driver.JdbcProfile

class Application extends Controller with HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val userDao = new UserDao(dbConfig)

  val form = Form(
    tuple(
      "name" -> nonEmptyText,
      "mail" -> nonEmptyText
    )
  )

  def index = Action.async { implicit request =>
    userDao.findAll().map { users =>
      Ok(views.html.index(form, users))
    }
  }

  def create = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors => {
        println(errors)
        userDao.findAll().map { users =>
          BadRequest(views.html.index(form, users))
        }
      },
      t => {
        val user = User(id = None, name = t._1, mail = t._2)
        userDao.insert(user).map(_ => Redirect(routes.Application.index))
      }
    )
  }

}
