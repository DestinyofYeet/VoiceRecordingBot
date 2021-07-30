package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.filesharing.SharePermissions;
import org.aarboard.nextcloud.api.filesharing.ShareType;

import java.io.File;
import java.io.IOException;

public class FileUpload {
    private final File fileToUpload;
    private final Config config = Main.config;
    private NextcloudConnector connector;
    private final Debugger debugger;
    private String fileUrl = null;
    private boolean isUploaded = false;

    public FileUpload(File fileToUpload, Debugger debugger){
        this.fileToUpload = fileToUpload;
        this.debugger = debugger;

        connect();
        upload();
    }

    private void connect(){
        connector = new NextcloudConnector(config.getCloudDomain(), true, 443, config.getCloudEmail(), config.getCloudPassword());
        debugger.debug("Established connection to the server");
    }

    private void upload(){
        try{
            debugger.debug("Filename: " + fileToUpload.getName());
            connector.uploadFile(fileToUpload, Main.config.getCloudUploadPath() + fileToUpload.getName());
            isUploaded = true;
            debugger.debug("Uploaded file " + fileToUpload.getName() + " to " + Main.config.getCloudUploadPath() + fileToUpload.getName());
            fileUrl = connector.doShare(Main.config.getCloudUploadPath() + fileToUpload.getName(), ShareType.PUBLIC_LINK, null, false, null, null).getUrl();
            shutdown();
        } catch (NextcloudApiException lol){

            shutdown();

            lol.printStackTrace();
        }

    }

    private void shutdown(){
        try {
            connector.shutdown();
            debugger.debug("Closing connection!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public boolean isUploaded() {
        return isUploaded;
    }
}
