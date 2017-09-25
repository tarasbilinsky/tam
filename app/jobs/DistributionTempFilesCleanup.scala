package jobs

import java.util.Date
import javax.inject.Inject

import base.controllers.EnvironmentAll
import base.jobs.{Job, JobModule}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import models.{ModelPlaceholders => PH}
import net.oltiv.scalaebean.Shortcuts._

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps
import base.MyConfigImplicit._
import com.amazonaws.regions.Regions

class DistributionTempFilesCleanup @Inject()(implicit env: EnvironmentAll) extends Job(nextRunIn = 5 minutes, every = 1 hours)({
  val s:Int = env.config.appTempAssetsTimeoutSeconds
  val moreThanXHourOld:Date = new Date(System.currentTimeMillis()-s*1000)

})

class DistributionTempFilesCleanupJobs extends JobModule(List(classOf[DistributionTempFilesCleanup]))