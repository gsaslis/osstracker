/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.oss.tools.osstrackerscraper

import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import org.slf4j.LoggerFactory
import play.api.libs.json._
import org.joda.time.{DateTime, DateTimeZone}
import java.util.Date

import com.netflix.oss.tools.osstrackerscraper.OssLifecycle.OssLifecycle

import scala.collection.mutable.ListBuffer

class BoxComScraper(boxRootFolder: String, boxComToken: String, esHost: String, esPort: Int, reportWriter: ReportWriter) {
  def logger = LoggerFactory.getLogger(getClass)
  val now = new DateTime().withZone(DateTimeZone.UTC)
  val dtfISO8601 = ISODateTimeFormat.dateTimeNoMillis()
  val dtfSimple = DateTimeFormat.forPattern("yyyy-MM-dd")
  def asOfISO = dtfISO8601.print(now)
  def asOfYYYYMMDD = dtfSimple.print(now)

  def updateElasticSearch(): Boolean = {

    val es = new ElasticSearchAccess(esHost, esPort)
    val box = new BoxComAccess(asOfYYYYMMDD, asOfISO, true, boxComToken)


      val fileStats = List[JsObject]()
      val filesList = box.countDownloadsOfAllFilesInFolder(boxRootFolder)

      for( file <- filesList ) {
        val projectName = (file \ "project_name").get.asInstanceOf[JsString].value
        val alreadyExistsDoc = es.getESDocForBoxStats(asOfYYYYMMDD, projectName)

        if (alreadyExistsDoc.isEmpty) {
          val indexed = es.indexDocInES("/osstracker/box_stats", file.toString())
          if (!indexed) {
            return false
          }
          logger.debug(s"indexed data for box.com ${projectName}")

        } else {
          logger.info(s"skipping up index of box.com doc for ${projectName}, ${asOfYYYYMMDD}")
        }
      }


    true
  }
}