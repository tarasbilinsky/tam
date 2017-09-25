import com.google.inject.AbstractModule
import java.time.Clock

import services.{ApplicationTimer, AtomicCounter, Counter}

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    bind(classOf[Counter]).to(classOf[AtomicCounter])
  }

}
