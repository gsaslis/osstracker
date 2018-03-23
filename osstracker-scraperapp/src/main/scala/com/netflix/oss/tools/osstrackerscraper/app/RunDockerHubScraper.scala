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
package com.netflix.oss.tools.osstrackerscraper.app

import com.netflix.oss.tools.osstrackerscraper.{BoxComScraper, DockerHubScraper}
import org.slf4j.LoggerFactory

object RunDockerHubScraper {
  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {

    val esHost = System.getenv("ES_HOST")
    val esPort = System.getenv("ES_PORT").toInt

    val dockerHubOrg = System.getenv("DOCKER_HUB_ORG")

    val scraper = new DockerHubScraper(dockerHubOrg, esHost, esPort, ConsoleReportWriter)
      val success = scraper.updateElasticSearch()
      if (!success) {
        System.exit(1)
      }
      logger.info(s"successfully updated the elastic search info for box.com downloads")
  }
}