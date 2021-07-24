package de.uwuwhatsthis.voiceRecorderBotForClara.commands;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Args;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.FileUpload;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;

public class UploadTest {

    public void execute(MessageReceivedEvent event, Args args){
        new FileUpload(new File("data/recorded.mp3"));
    }
}
