package dependency.resolver

import scala.annotation.tailrec

case class DependencyResolution(
  resolved: List[Seq[ProjectInfo]],
  circular: Seq[ProjectInfo],
) {

  private[this] lazy val allResolvedLibraries: Set[String] = resolved.flatMap(_.flatMap { p => p.provides.map(_.libraryId) }).toSet

  def isLibraryResolved(libraryId: String): Boolean = allResolvedLibraries.contains(libraryId)

}

object DependencyResolution {
  val empty: DependencyResolution = DependencyResolution(resolved = Nil, circular = Nil)
}

case class LibraryReference(libraryId: String)

case class ProjectInfo(
  projectId: String,
  dependsOn: Seq[LibraryReference],
  provides: Seq[LibraryReference],
)

/**
 * Given a list of projects, resolves the dependencies among them
 * to return an ordered list of sets of projects that can be upgraded
 * at one time.
 */
case class DependencyResolver() {

  def resolve(projects: Seq[ProjectInfo]): DependencyResolution = {
    resolve(DependencyResolution.empty, projects.toList)
  }

  private[this] def areDependenciesSatisfied(resolution: DependencyResolution, libraries: Seq[LibraryReference]): Boolean = {
    libraries.forall { l =>
      resolution.isLibraryResolved(l.libraryId)
    }
  }

  @tailrec
  private[this] def resolve(resolution: DependencyResolution, projects: List[ProjectInfo]): DependencyResolution = {
    val (resolved, remaining) = projects.partition { p =>
      areDependenciesSatisfied(resolution, p.dependsOn)
    }

    resolved match {
      case Nil => resolution.copy(
        circular = resolution.circular ++ remaining,
      )
      case _ => {
        resolve(resolution.copy(
          resolved = resolution.resolved ++ Seq(resolved),
        ), remaining)
      }
    }
  }
}
