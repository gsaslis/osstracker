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
import org.joda.time.{DateTime, DateTimeZone}
import org.slf4j.LoggerFactory
import play.api.libs.json.JsString

class DockerHubScraper(dockerHubOrg: String, esHost: String, esPort: Int, reportWriter: ReportWriter) {
  def logger = LoggerFactory.getLogger(getClass)
  val now = new DateTime().withZone(DateTimeZone.UTC)
  val dtfISO8601 = ISODateTimeFormat.dateTimeNoMillis()
  val dtfSimple = DateTimeFormat.forPattern("yyyy-MM-dd")
  def asOfISO = dtfISO8601.print(now)
  def asOfYYYYMMDD = dtfSimple.print(now)

  def updateElasticSearch(): Boolean = {

    val es = new ElasticSearchAccess(esHost, esPort)
    val box = new DockerHubAccess(asOfYYYYMMDD, asOfISO, true, dockerHubOrg)



      val imagesList = box.countDownloadsOfAllDockerImages(dockerHubOrg)

      for( image <- imagesList ) {
        val imageName = (image \ "image_name").get.asInstanceOf[JsString].value

        val alreadyExistsDoc = es.getESDocForDockerHubStats(asOfYYYYMMDD, imageName)

        if (alreadyExistsDoc.isEmpty) {
          val indexed = es.indexDocInES("/osstracker/docker_stats", image.toString())
          if (!indexed) {
            return false
          }

        } else {
          logger.info(s"skipping up index of docker hub doc for ${imageName}, ${asOfYYYYMMDD}")
        }
      }


    true
  }
}