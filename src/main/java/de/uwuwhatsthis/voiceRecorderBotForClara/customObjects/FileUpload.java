package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import org.aarboard.nextcloud.api.NextcloudConnector;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;

import java.io.File;
import java.io.IOException;

public class FileUpload {
    private final File fileToUpload;
    private final Config config = Main.config;
    private NextcloudConnector connector;

    public FileUpload(File fileToUpload){
        this.fileToUpload = fileToUpload;

        connect();
        upload();
    }

    private void connect(){
        connector = new NextcloudConnector(config.getCloudDomain(), true, 443, config.getCloudEmail(), config.getCloudPassword());
        System.out.println("login successful");
    }

    private void upload(){
        try{
            System.out.println(connector.listFolderContent("/"));
        } catch (NextcloudApiException lol){
            try {
                connector.shutdown();
                System.out.println("shutdown");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
