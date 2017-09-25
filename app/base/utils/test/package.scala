package base.utils

import net.oltiv.scalaebean.Shortcuts._
import io.ebean.EbeanServerFactory
import io.ebean.common.BeanSet
import io.ebean.config.ServerConfig
import models.{User, UserRole}

package object test {

  def setUpTestORM(name: String = "mem") = {
    val config:ServerConfig = new ServerConfig
    config.setName(name)
    config.loadTestProperties
    config.setDefaultServer(true)
    config.setRegister(true)
    EbeanServerFactory.create(config)

    transaction{implicit db =>
      val user = new User
      user.name = "John Brown"
      val role = new UserRole
      role.sysName="admin"
      role.title="Admin"
      role.active=true
      role.save()
      user.save()
      user.roles = new BeanSet[UserRole]
      user.roles.add(role)
    }
  }

}
