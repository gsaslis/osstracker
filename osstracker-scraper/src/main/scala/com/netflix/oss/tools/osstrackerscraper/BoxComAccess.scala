package com.netflix.oss.tools.osstrackerscraper

import com.box.sdk.{BoxAPIConnection, BoxFile, BoxFolder}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, Json}
import com.box.sdk.BoxAPIConnection
      import com.box.sdk.BoxFolder
      import com.box.sdk.BoxItem

import scala.collection.JavaConversions._


  class BoxComAccess(val asOfYYYYMMDD: String, val asOfISO: String, connectToBox: Boolean, boxComToken: String) {

    val logger = LoggerFactory.getLogger(getClass)
    val boxCom: Option[BoxAPIConnection] = if (connectToBox) Some(new BoxAPIConnection(boxComToken)) else None


    val clientId = "k05nwgjkkkijj0l8ivz2ox1zgev9mo3l"
    val clientSecret = "EghpUkPRCdnnkR0LoxBQofmyb4s6m0UD"

//    var authenticator = new com.box.sdk.TokenProvider(clientId, clientSecret);
//                    var oAuthToken = authenticator.RefreshAccessToken(refreshToken);
//                    accessToken = oAuthToken.AccessToken;
//                    BoxTool.Properties.Settings.Default.accessToken = oAuthToken.AccessToken;
//                    BoxTool.Properties.Settings.Default.refreshToken = oAuthToken.RefreshToken;
//                    var boxManager = new BoxManager(accessToken);
//
//
//    public BoxDeveloperEditionAPIConnection(String entityId,
//                                            DeveloperEditionEntityType entityType,
//                                            String clientID,
//                                            String clientSecret,
//                                            JWTEncryptionPreferences encryptionPref,
//                                            IAccessTokenCache accessTokenCache)
//
//
//
//    Folder folder = boxManager.GetFolder("FolderId");
//    BoxApi.V2.Model.Folder folders = folder.Folder.Single(f => f.Name.Equals("FolderName"));

//    val api = new BoxAPIConnection(
//      "k05nwgjkkkijj0l8ivz2ox1zgev9mo3l",
//      "EghpUkPRCdnnkR0LoxBQofmyb4s6m0UD",
//      "gASfGOAH9GKUnptDD2nVHzg3Owlr2m9a",
//      "YOUR-REFRESH-TOKEN"
//    )


    def countDownloadsOfAllFilesInFolder(folderId: String): List[JsObject] = {

      val folder = new BoxFolder(boxCom.get, folderId)

      var fileStats = List[JsObject]()
      for (fileInfo <- folder.getChildren) {
        fileInfo match {
          case _: BoxFile#Info =>
            logger.info(s"processing file: ${fileInfo.getName}")
            val boxFile = new BoxFile(boxCom.get, fileInfo.getID)

            val boxFilePaths = boxFile.getInfo.getPathCollection

            val releaseName = boxFilePaths.last.getName
            val projectName = boxFilePaths.get(boxFilePaths.size()-2).getName // last is -1, second to last is -2


            val boxDownloadsJson = Json.obj(
              "asOfISO" -> asOfISO,
              "asOfYYYYMMDD" -> asOfYYYYMMDD,
              "project_name" -> projectName,
              "release_name" -> releaseName,
              "total_downloads_to_date" -> boxFile.getInfo.getSharedLink.getDownloadCount
            )
            logger.debug("repo json = " + boxDownloadsJson)
            fileStats = fileStats ::: List(boxDownloadsJson)

          case folderInfo: BoxFolder#Info =>
            logger.info(s"processing folder: ${folderInfo.getName}")
            fileStats = fileStats ::: countDownloadsOfAllFilesInFolder(folderInfo.getID)
          case _ =>

        }
      }
      fileStats
    }


}
