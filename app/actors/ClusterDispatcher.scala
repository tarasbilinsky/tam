package actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import base.MyConfigImplicit._
import base.controllers.EnvironmentAll
import models.{Node, ModelPlaceholders => PH}
import net.oltiv.scalaebean.Shortcuts._
import org.bouncycastle.util.test.Test
import play.Logger

import scala.concurrent.duration._
import scala.language.postfixOps

object ClusterDispatcher {
  trait ActorKey
  case object WaitConference extends ActorKey
  case object RunConference extends ActorKey

  case class Join(nodeId: Long)
  case class Leave(nodeId: Long)
  case class Forward(k: ActorKey, msg: Any,addNodeId:Boolean = false)
  case class AskAll(k: ActorKey, msg: Any)
  case class AskForward(k: ActorKey, msg: Any, replyTo: ActorRef)
  case class MessageWithNodeId(msg:Any,nodeId:Option[Long])
  case object SetUpId
  val thePath = "cluster-dispatcher"

}

class ClusterDispatcherActor(dispatcher: ClusterDispatcher, remotingOn: Boolean, hostIp: String, deployGroup: Int) extends Actor with ActorLogging{
  import ClusterDispatcher._
  private var members:Set[(Node,ActorSelection)] = Set()
  private var myId:Option[Long] = None


  override def receive: Receive = {
    case SetUpId =>
      myId = Some(Node.findByIp(hostIp, deployGroup).id)
      query(PH.node, PH.node.deployGroup == deployGroup, PH.node.id).seq().foreach { n =>
        self ! Join(n.id)
      }
    case Join(newNodeId) =>
      myId.filter( _ != newNodeId).foreach {id =>
        def getById(nodeId:Long):Option[(Node,ActorSelection)] = query(PH.node,PH.node.id==nodeId,PH.node.id, PH.node.ipv4).one().map(n=> (n, dispatcher.env.akka.actorSelection(s"akka.tcp://application@${n.ipv4}:${dispatcher.env.config.hostPort}/user/${thePath}")))
        getById(newNodeId).foreach{ m =>
          val mUpdated = members + m
          if (mUpdated != members) {//optimized exists check
            members = mUpdated
            Logger.error(s"Joined ${m._1.id} ${m._1.ipv4} ${m._1.deployGroup}")
            Logger.error(s"myId=$id")
            m._2 ! Join(id)
          }
        }
      }

    case Leave(nodeId) =>
      members = members.filter(_._1.id!=nodeId)

    case Forward(k,msg,addNodeId) =>
      val msgWithKey =  (k,msg,addNodeId)
      self ! msgWithKey
      members.foreach(_._2 ! msgWithKey)

    case AskAll(k,msg) =>
      val msgWithKeyAndReplyTo = AskForward(k,msg,sender())
      self ! msgWithKeyAndReplyTo
      members.foreach(_._2 ! msgWithKeyAndReplyTo)

    case (k: ActorKey, msg: Any, addNodeId: Boolean) => dispatcher.actors(k) ! (if(!addNodeId) msg else MessageWithNodeId(msg,myId))

    case a @ AskForward(k,_,_) => dispatcher.actors(k) ! a

    case message @ _ => log.error("Unexpected message to ClusterDispatcherActor")
  }

  override def postStop(): Unit = {
    for{
      id <- myId
      member <- members
    } {
      member._2 ! Leave(id)
      Node.deleteById(id)
    }
    super.postStop()
  }

  override def preStart(): Unit = {
    if(remotingOn) {
        context.system.scheduler.scheduleOnce(3 second){self ! SetUpId}(context.dispatcher)
    }
    super.preStart()
  }
}

class ClusterDispatcher(implicit val env: EnvironmentAll){
  import ClusterDispatcher._
  val selfRef: ActorSelection = env.akka.actorSelection(s"akka://application/user/$thePath")
  val dispatcherActor:ActorRef = env.akka.actorOf(Props(new ClusterDispatcherActor(this,env.config.remotingOn,env.config.hostIp,env.config.appDeployGroup)),thePath)
  //val waitConfActor: ActorRef = env.akka.actorOf(Props(new ConferenceDispatcher(this)),"conference-dispatcher-root")
  val actors:Map[ActorKey,ActorRef] = Map(
    //WaitConference -> waitConfActor,
    //RunConference -> env.akka.actorOf(Props(new ConferenceRunDispatcher(this)),"conference-run-dispatcher-root")
  )

  def send(k: ActorKey,msg: Any,addNodeId:Boolean=false): Unit = {
    dispatcherActor ! Forward(k, msg,addNodeId)
  }
}

