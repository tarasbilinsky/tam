package models.feedback

import net.oltiv.scalaebean.Shortcuts._
import models.{ModelPlaceholders=>PH}

object FeedbackTypes {
  def apply(areaId: Long):Seq[FeedbackType] = query(PH.feedbackType,PH.feedbackType.area.id==areaId && PH.feedbackType.active).order(props(PH.feedbackType,PH.feedbackType.orderNumber)).seq()
}
