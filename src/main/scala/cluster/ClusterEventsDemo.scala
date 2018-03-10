package cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster._
import com.typesafe.config.ConfigFactory

class EventLisener extends Actor with ActorLogging {
  val cluster = Cluster(context.system)
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember]) //订阅集群状态转换信息
    super.preStart()
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self) //取消订阅
    super.postStop()
  }

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info("Member is Joining: {}", member.address)
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case MemberLeft(member) =>
      log.info("Member is Leaving: {}", member.address)
    case MemberExited(member) =>
      log.info("Member is Exiting: {}", member.address)
    case MemberRemoved(member, previousStatus) =>
      log.info(
        "Member is Removed: {} after {}",
        member.address, previousStatus)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
      cluster.down(member.address) //手工驱除，不用auto-down
    case _: MemberEvent => // ignore
  }

}

object ClusterEventsDemo {
  def main(args: Array[String]): Unit = {
    //重设port,seed-node-address
    val port =
      if (args.isEmpty) "0"
      else args(0)

    val addr =
      if (args.length < 2) "2551"
      else args(1)

    val seednodeSetting = "akka.cluster.seed-nodes = [" +
      "\"akka.tcp://clusterSystem@127.0.0.1:" +
      s"${addr}" + "\"]"

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port = ${port}")
      .withFallback(ConfigFactory.parseString(seednodeSetting))
      .withFallback(ConfigFactory.load("cluster.conf"))

    val clusterSystem = ActorSystem(name = "clusterSystem", config = config)
    val eventListener = clusterSystem.actorOf(Props[EventLisener], "eventListener")

    val cluster = Cluster(clusterSystem)
    cluster.registerOnMemberRemoved(println("Leaving cluster. I should cleanup... "))
    cluster.registerOnMemberUp(println("Hookup to cluster. Do some setups ..."))
    println("actor system started!")
    scala.io.StdIn.readLine()
    cluster.leave(cluster.selfAddress)
    scala.io.StdIn.readLine()
    clusterSystem.terminate()

  }
}