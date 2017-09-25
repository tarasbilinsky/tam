package jobs

import javax.inject.Inject

import base.controllers.EnvironmentAll
import base.jobs._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.opentok.Archive
import models.{ModelPlaceholders => PH, _}

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.collection.JavaConverters._
import scala.collection.mutable
import net.oltiv.scalaebean.Shortcuts._
import base.MyConfigImplicit._


class ArchivesMove @Inject()(implicit env: EnvironmentAll) extends DailyJob(0,0,0)({

}) with CancelOnAppStop

class ArchivesMoveJobs extends JobModule(List(classOf[ArchivesMove]))