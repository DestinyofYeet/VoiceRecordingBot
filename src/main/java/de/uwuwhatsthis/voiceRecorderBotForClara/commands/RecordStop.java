package de.uwuwhatsthis.voiceRecorderBotForClara.commands;

import de.uwuwhatsthis.voiceRecorderBotForClara.audio.ReceiveAndHandleAudioForChannel;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Args;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Embed;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.Constants;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class RecordStop {

    public void execute(MessageReceivedEvent event, Args args){
        if (!event.getMember().hasPermission(RecordStart.PERMISSION_NEEDED)){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "Insufficient permissions! You need the " + RecordStart.PERMISSION_NEEDED.toString() + " permission to run this command!", Color.RED).build()).queue();
            return;
        }

        for (VoiceChannel vc: Constants.AUDIO_MAP.keySet()){
            if (vc.getGuild().getIdLong() == event.getGuild().getIdLong()){
                event.getChannel().sendMessageEmbeds(new Embed("Stopping recording...", "Stopping recording... Please wait", Color.GREEN).build()).queue();
                stopRecording(vc);
                event.getChannel().sendMessageEmbeds(new Embed("Stopped recording", "Stopped the recording in voicechannel " + vc.getAsMention() + "!", Color.GREEN).build()).queue();
                return;
            }
        }

        event.getChannel().sendMessageEmbeds(new Embed("Error", "The bot isn't recording anything on this server!", Color.RED).build()).queue();
    }

    public static void stopRecording(VoiceChannel vc){
        ReceiveAndHandleAudioForChannel audioHandler = Constants.AUDIO_MAP.get(vc);
        Constants.AUDIO_MAP.remove(vc);
        Constants.USER_ALLOWED_IN_CHANNEL.remove(vc);

        vc.getGuild().getAudioManager().setReceivingHandler(null);

        audioHandler.saveAudio();

        vc.getGuild().getAudioManager().closeAudioConnection();
    }
}
