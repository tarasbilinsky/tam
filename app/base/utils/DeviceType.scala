package base.utils

import play.api.mvc.WrappedRequest

class DeviceType(request: WrappedRequest[Any]) {
  private val userAgent = request.headers.get("User-Agent")
  def isIOS:Boolean = userAgent.exists(_.matches(".*(iPod|iPhone|iPad).*"))
  def isAndroid:Boolean = userAgent.exists(_.contains("Android"))
}
