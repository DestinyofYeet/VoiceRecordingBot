package de.uwuwhatsthis.voiceRecorderBotForClara.events;

import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.ConsentMessage;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Embed;
import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.managers.DebugManager;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.Constants;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildVoiceJoinEventListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event){
        if (event.getMember().getUser() == event.getJDA().getSelfUser()) return;
        if (event.getMember().getUser().isBot()) return;

        VoiceChannel voiceChannelJoined = event.getVoiceState().getChannel();

        if (!Constants.AUDIO_MAP.containsKey(voiceChannelJoined)){
            return;
        }

        if (!helper.hasConsented(voiceChannelJoined, event.getMember().getUser())){
            // if the user didn't consent yet, kick the user, sent the user a consent message
            event.getGuild().moveVoiceMember(event.getMember(), null).queue();
            event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                new ConsentMessage(privateChannel, voiceChannelJoined, Main.debugManager.getDebugger(event.getGuild()), true);
            });
        }
    }
}
