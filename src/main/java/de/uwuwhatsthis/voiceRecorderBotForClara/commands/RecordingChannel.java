package de.uwuwhatsthis.voiceRecorderBotForClara.commands;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Args;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Cache;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Embed;
import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class RecordingChannel {

    public void execute(MessageReceivedEvent event, Args args){
        if (!event.getMember().hasPermission(RecordStart.PERMISSION_NEEDED)){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "Insufficient permissions! You need the " + RecordStart.PERMISSION_NEEDED.toString() + " permission to run this command!", Color.RED).build()).queue();
            return;
        }

        if (args.isEmpty()){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "You need to provide a channel to send the logs into!", Color.RED).build()).queue();
            return;
        }

        TextChannel channel = helper.getTextChannelById(event.getGuild(), args.get(0));

        if (channel == null){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "The text channel was not found!", Color.RED).build()).queue();
            return;
        }

        Cache cache = Main.cacheManager.getCacheForServer(event.getGuild().getId());

        cache.setRecordingLogChannelId(channel.getId());
        Main.cacheManager.saveCaches();

        event.getChannel().sendMessageEmbeds(new Embed("Set a log channel!", "Successfully set the recording log channel to " + channel.getAsMention() + "!", Color.GREEN).build()).queue();
    }
}
