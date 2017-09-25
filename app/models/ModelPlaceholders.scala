package models

import base.models._
import models.feedback._


object ModelPlaceholders {
  val user = new User
  val userRole = new UserRole
  val lookup = new Lookup{}
  val modelBase = new ModelBase{}
  val feedbackArea = new FeedbackArea
  val feedbackType = new FeedbackType
  val feedback = new Feedback
  val state = new State
  val node = new Node
}
