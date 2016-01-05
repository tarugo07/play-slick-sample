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
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "mail" -> email
    )(User.apply)(User.unapply)
  )

  def index = Action.async { implicit request =>
    userDao.findAll().map { users =>
      Ok(views.html.index(users))
    }
  }

  def add = Action { implicit request =>
    Ok(views.html.add(form))
  }

  def edit(id: Long) = Action.async { implicit request =>
    userDao.findById(id).map { userOpt =>
      userOpt.map { user =>
        Ok(views.html.edit(form.fill(user)))
      }.getOrElse(NotFound)
    }
  }

  def create = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors =>
        Future.successful(BadRequest(views.html.add(errors))),
      user =>
        userDao.insert(user).map(_ => Redirect(routes.Application.index()))
    )
  }

  def update = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors =>
        Future.successful(BadRequest(views.html.edit(errors))),
      user =>
        userDao.update(user).map(_ => Redirect(routes.Application.index()))
    )
  }

}
