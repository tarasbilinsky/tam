package base.controllers

import base.models.{PermissionBase, UserBase, UserRoleBase, UserSessionBase}
import play.api.Logger
import play.api.mvc.Security.{AuthenticatedBuilder, AuthenticatedRequest}
import play.api.mvc.{ActionBuilder, _}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.reflect.ClassTag


trait Secure[S<:UserSessionBase[U], U<:UserBase, R<: UserRoleBase, P<:PermissionBase] extends ControllerBase{

  /***
    *  Constants
    */

  val notAuthorizedPageRedirect: String = "/login"
  val userCachingDuration: Duration  = 5 minutes
  private val idInSession = "id"
  private val logMarkerStart =
    """
      |
      |
      |
      |
      |
    """.stripMargin
  @inline
  private def logStart() = Logger.debug(logMarkerStart)

  /****
    * Secure Actions
    */

  type MRQ[A] = MayBeSecureRequest[A,U]
  type SRQ[A] = SecureRequest[A,U]



  def actionMayBeSecure(action: => MRQ[AnyContent] => Result)(implicit cS: ClassTag[S], cU: ClassTag[U]):EssentialAction ={
    val ab = new ActionBuilder[MRQ,AnyContent] with ActionTransformer[Request, MRQ] {
      override protected def executionContext: ExecutionContext = controllerComponents.executionContext
      override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
      def transform[A](request: Request[A]): Future[MayBeSecureRequest[A, U]] = Future.successful {
        logStart()
        new MayBeSecureRequest(getUser(request), request)
      }
    }
    ab(controllerComponents.parsers.defaultBodyParser)(action)
  }
  def actionSecure(action: => SRQ[AnyContent] => Result)(implicit cS: ClassTag[S], cU: ClassTag[U]):EssentialAction = {
    val ab = new ActionBuilder[SRQ,AnyContent] {
      override protected def executionContext: ExecutionContext = controllerComponents.executionContext
      override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
      def invokeBlock[A](request: Request[A], block: (SRQ[A]) => Future[Result]): Future[Result] = {
        AuthenticatedBuilder(
          getUser,
          controllerComponents.parsers.defaultBodyParser,
          _ => Redirect(notAuthorizedPageRedirect))(controllerComponents.executionContext)

          .authenticate(request, { authRequest: AuthenticatedRequest[A, U] =>
            logStart()
            block( new SecureRequest[A,U](authRequest.user, request))
          })
      }
    }
    ab(controllerComponents.parsers.defaultBodyParser)(action)
  }

  def actionSecureByRole(roles: Long*)(action: => SRQ[AnyContent] => Result)(implicit cS: ClassTag[S], cU: ClassTag[U]): EssentialAction = {
    actionSecureByPermissions(roles: _*)()(action)
  }

  def actionSecureByPermissions(roles: Long*)(permissions: Long*)(action: => SRQ[AnyContent] => Result)(implicit cS: ClassTag[S], cU: ClassTag[U]): EssentialAction = {
    val ab = new ActionBuilder[SRQ,AnyContent] {
      override protected def executionContext: ExecutionContext = controllerComponents.executionContext
      override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
      def invokeBlock[A](request: Request[A], block: (SRQ[A]) => Future[Result]): Future[Result] = {
        AuthenticatedBuilder(getUser(roles: _*)(permissions: _*),
          controllerComponents.parsers.defaultBodyParser,
          _ => Redirect(notAuthorizedPageRedirect))(controllerComponents.executionContext)

          .authenticate(request, { authRequest: AuthenticatedRequest[A, U] =>
            logStart()
            block( new SecureRequest[A,U](authRequest.user, request))
          })
      }
    }
    ab(controllerComponents.parsers.defaultBodyParser)(action)
  }

  def actionSecureWithUser(action: => U => Request[AnyContent] => Result)(implicit cS: ClassTag[S], cU: ClassTag[U]): EssentialAction =
    Security.Authenticated(getUser,  _ => Redirect(notAuthorizedPageRedirect))
    { user => actionMayBeSecure(request => action(user)(request)) }


  /***
    *
    * Helpers
    */
  def initSession(user: U)(implicit cS: ClassTag[S], cU: ClassTag[U]):(String,String) = {
    val sessionClass = cS.runtimeClass
    val userClass = cU.runtimeClass
    val sessionConstructor = sessionClass.getDeclaredConstructor(userClass)
    val us = sessionConstructor.newInstance(user)
    val session:S = us match {
      case s:S => s
      case _ => throw new RuntimeException("unexpected")
    }
    env.userCache.set(session.getIdString,session,userCachingDuration)
    (idInSession,session.getIdString)
  }

  def getSession(request: RequestHeader)(implicit cS: ClassTag[S]): Option[S] = request.session.get(idInSession).flatMap{ id:String =>
    val s:Option[S] = env.userCache.get(id)
    val s2:Option[S]  = s.fold{
      val m = cS.runtimeClass.getDeclaredMethod("restore",classOf[String])
      val resS: Option[S] = m.invoke(null,id) match{case s: S=> Some(s); case _ => None}//= UserSession.restore(id)
      resS.foreach(x=>env.userCache.set(x.getIdString,resS,userCachingDuration))
      resS
    } (x=>Some(x))
    s2
  }

  def getUser(requiredRole: Long*)(requiredPermission: Long*)(request: RequestHeader)(implicit cS: ClassTag[S], cU: ClassTag[U]):Option[U] = {
    val user = getUser(request)
    val userWithRolePass = user.filter {user => requiredRole.exists(user.getRoles.contains(_))}
    val userWithRoleAndPermissionsPass = userWithRolePass.filter{user => user.getPermissions.containsAll(scala.collection.JavaConverters.seqAsJavaList(requiredPermission))}
    userWithRoleAndPermissionsPass
  }

  def getUser(request: RequestHeader)(implicit cS: ClassTag[S]):Option[U] = {
    getSession(request).flatMap{s2 => Option(s2.getUser)}
  }

}

class SecureRequest[A, U<:UserBase](val user: U, request: Request[A]) extends WrappedRequest[A](request)

class MayBeSecureRequest[A, U<:UserBase](val user: Option[U], request: Request[A]) extends WrappedRequest[A](request)

object RequestWrapperForTemplates{
  class GenericRequest[A](request: Request[A]){
    def getUser:Option[UserBase] = request match {
      case sr:SecureRequest[A,_] => Some(sr.user)
      case mr:MayBeSecureRequest[A,_] => mr.user
      case _ => None
    }
  }
  implicit def requestToGenericRequest[A](request: Request[A]):GenericRequest[A] = new GenericRequest[A](request)
}





