package de.uwuwhatsthis.voiceRecorderBotForClara.commands;

import de.uwuwhatsthis.voiceRecorderBotForClara.audio.ReceiveAndHandleAudioForChannel;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Args;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Embed;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.Constants;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordStart {
    public final static Permission PERMISSION_NEEDED = Permission.ADMINISTRATOR;

    public void execute(MessageReceivedEvent event, Args args){
        if (!event.getMember().hasPermission(PERMISSION_NEEDED)){
            event.getChannel().sendMessageEmbeds(new Embed("Insufficient Permissions", "Insufficient permissions! You need the " + PERMISSION_NEEDED.toString() + " for this command!", Color.RED).build()).queue();
            return;
        }

        if (args.isEmpty()){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "You need to specify a voice channel to record!", Color.RED).build()).queue();
            return;
        }

        String voiceChannelInput = args.get(0);

        VoiceChannel voiceChannel = helper.getVoiceChannelById(event.getGuild(), voiceChannelInput);

        if (voiceChannel == null){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "The voice channel could not be found!", Color.RED).build()).queue();
            return;
        }

        if (voiceChannel.getMembers().isEmpty()){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "The channel you are trying to record is empty!", Color.RED).build()).queue();
            return;
        }

        AtomicBoolean shouldExit = new AtomicBoolean(false);

        Constants.AUDIO_MAP.forEach((VoiceChannel, ReceiveAndHandleAudioForChannel) -> {
            if (VoiceChannel.getGuild().getIdLong() == event.getGuild().getIdLong()){
                event.getChannel().sendMessageEmbeds(new Embed("Error", "The bot is already recording channel " + VoiceChannel.getAsMention() + "!", Color.RED).build()).queue();
                shouldExit.set(true);
            }
        });

        if (shouldExit.get()) return;

        ReceiveAndHandleAudioForChannel audioMananger = new ReceiveAndHandleAudioForChannel(voiceChannel, event);

        Constants.AUDIO_MAP.put(voiceChannel, audioMananger);

        // event.getChannel().sendMessageEmbeds(new Embed("Started recording!", "Started recording in channel " + voiceChannel.getAsMention() + "!", Color.GREEN).build()).queue();
    }
}
