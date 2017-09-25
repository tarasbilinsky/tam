package base

import base.jobs.Job
import com.google.inject.AbstractModule
import io.ebean.EbeanServer

class EbeanModule extends AbstractModule{
  def configure(): Unit = bind(classOf[EbeanServer]).toProvider(classOf[EbeanServerProvider]).asEagerSingleton()
}
