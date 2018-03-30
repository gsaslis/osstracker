package com.netflix.oss.tools.osstrackerscraper

import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

import scalaj.http.{Http, HttpOptions}


class DockerHubAccess(val asOfYYYYMMDD: String, val asOfISO: String, connectToBox: Boolean, boxComToken: String) {

  val logger = LoggerFactory.getLogger(getClass)

  def countDownloadsOfAllDockerImages(dockerHubOrganization: String): List[JsObject] = {



    var imageStats = List[JsObject]()
    val dockerHubResponse = Http(s"https://registry.hub.docker.com/v2/repositories/${dockerHubOrganization}")
      .option(HttpOptions.followRedirects(true)).execute()

    val dockerHubJsonResponse = Json.parse(dockerHubResponse.body.toString)
    val results = (dockerHubJsonResponse \ "results").get.asInstanceOf[JsArray]


    for (image <- results.value) {

      val imageName = (image \ "name").get.asInstanceOf[JsString].value
          logger.info(s"processing image: ${imageName}")

          val boxDownloadsJson: JsObject = Json.obj(
            "asOfISO" -> asOfISO,
            "asOfYYYYMMDD" -> asOfYYYYMMDD,
            "image_name" -> imageName,
            "star_count" -> (image \ "star_count").get,
            "pull_count" -> (image \ "pull_count").get
          )
          logger.debug("repo json = " + boxDownloadsJson)
          imageStats = boxDownloadsJson :: imageStats

    }
    imageStats
  }


}
