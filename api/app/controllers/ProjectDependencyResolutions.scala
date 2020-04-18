package controllers

import db.{Authorization, MembershipsDao}
import io.flow.common.v0.models.UserReference
import io.flow.dependency.v0.models.json._
import io.flow.play.controllers.FlowControllerComponents
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import io.flow.util.Config
import play.api.libs.json.Json
import services.ProjectDependencyResolutionService

@Singleton
class ProjectDependencyResolutions @Inject() (
  val config: Config,
  val controllerComponents: ControllerComponents,
  val flowControllerComponents: FlowControllerComponents,
  val baseIdentifiedControllerWithFallbackComponents: BaseIdentifiedControllerWithFallbackComponents,
  membershipsDao: MembershipsDao,
  service: ProjectDependencyResolutionService,
) extends BaseIdentifiedControllerWithFallback {

  def get(organization: String): Action[AnyContent] = IdentifiedWithFallback { request =>
    withValidatedMember(request.user, organization) {
      Ok(
        Json.toJson(
          service.getByOrganizationId(organization)
        )
      )
    }
  }

  private[this] def withValidatedMember(user: UserReference, organizationId: String)(f: => Result): Result = {
    membershipsDao.findByOrganizationAndUserId(Authorization.All, organizationId, user.id) match {
      case None => Unauthorized
      case Some(_) => f
    }
  }
}
