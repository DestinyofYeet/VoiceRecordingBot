package de.uwuwhatsthis.voiceRecorderBotForClara.customObjects;

import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff.ReactionEmotes;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.helper;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.awt.*;

public class ConsentMessage implements Runnable {

    private PrivateChannel privateChannel;
    private VoiceChannel voiceChannel;
    private Debugger debugger;
    private boolean joinedAfterInit = false;

    public ConsentMessage(PrivateChannel privateChannel, VoiceChannel voiceChannel, Debugger debugger){
        this.privateChannel = privateChannel;
        this.voiceChannel = voiceChannel;
        this.debugger = debugger;

        Thread thread = new Thread(this);
        thread.start();
    }

    public ConsentMessage(PrivateChannel privateChannel, VoiceChannel voiceChannel, Debugger debugger, boolean joinedAfterInit){
        this.privateChannel = privateChannel;
        this.voiceChannel = voiceChannel;
        this.debugger = debugger;
        this.joinedAfterInit = joinedAfterInit;

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        privateChannel.sendMessageEmbeds(
                new Embed("Channel recording", "The voice channel \"" + voiceChannel.getName() + "\" on server \"" + voiceChannel.getGuild().getName() + "\" is " + (joinedAfterInit ? "currently being recorded" : "about to be recorded") + ".\n Please either consent to this recording or not. If you do not consent, you " +  (joinedAfterInit ? "will not be able to join the voice channel" : "will be kicked from the voice channel") + "! \n \n**You have 60 seconds to consent!**", Color.GREEN)
                        .build()
        ).queue(message -> {
            Main.reactionManager.queue(message, ReactionEmotes.CHECK_MARK_EMOTE, (message1, user) -> {
                // when user reacts with a check mark
                debugger.debug(user.getAsTag() + " has consented to being recorded!");
                helper.addMemberToAllowedChannelList(voiceChannel, user);
                message1.editMessageEmbeds(new Embed("Consented", "You successfully consented to be recorded!", Color.GREEN).build()).queue();
                Main.reactionManager.clearQueueForMessageID(message.getIdLong());
            });

            Main.reactionManager.queue(message, ReactionEmotes.RED_CROSS_EMOTE, ((message1, user) -> {
                // when the user declines / reacts with the check mark
                debugger.debug(user.getAsTag() + " has not consented to being recorded!");
                message1.editMessageEmbeds(new Embed("Did not consent", "You did not consent to being recorded and therefore will be removed from the voice channel once the 60 seconds have passed!", Color.RED).build()).queue();
                helper.removeMemberFromAllowedChannelList(voiceChannel, user);
                Main.reactionManager.clearQueueForMessageID(message.getIdLong());
            }));

            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Main.reactionManager.clearQueueForMessageID(message.getIdLong());

            if (joinedAfterInit){
                message.editMessageEmbeds(new Embed("Did not consent", "Your time expired. You did not consent and cannot join the voice channel!", Color.RED).build()).queue();
            }
        });
    }
}
