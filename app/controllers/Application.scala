package controllers

import javax.inject.Inject

import infrastructure.UserDao
import model.User
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.concurrent.Future

class Application @Inject()(val messagesApi: MessagesApi)
  extends Controller
  with HasDatabaseConfig[JdbcProfile]
  with I18nSupport {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val userDao = new UserDao(dbConfig)

  val form = Form(
    tuple(
      "name" -> nonEmptyText,
      "mail" -> email
    )
  )

  def index = Action.async { implicit request =>
    userDao.findAll().map { users =>
      Ok(views.html.index(users))
    }
  }

  def add = Action { implicit request =>
    Ok(views.html.add(form))
  }

  def create = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors =>
        Future.successful(BadRequest(views.html.add(errors))),
      input => {
        val user = User(id = None, name = input._1, mail = input._2)
        userDao.insert(user).map(_ => Redirect(routes.Application.index))
      }
    )
  }

}
