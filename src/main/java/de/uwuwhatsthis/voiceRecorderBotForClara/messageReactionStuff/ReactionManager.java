package de.uwuwhatsthis.voiceRecorderBotForClara.messageReactionStuff;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReactionManager extends ListenerAdapter {

    private final BlockingQueue<ReactionUnit> queue = new LinkedBlockingQueue<>();

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event){
        if (event.getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        for (ReactionUnit unit: queue){
            if (unit.getMessage().getIdLong() == event.getMessageIdLong()) {

                if (unit.getEmote() != null) {
                    if (event.getReactionEmote().getIdLong() != unit.getEmote().getIdLong()) continue;
                }

                if (unit.getReactionEmote() != null) {
                    if (!event.getReactionEmote().getEmoji().equals(unit.getReactionEmote().getEmoji())) continue;
                }
                unit.getLambda().execute(event.retrieveMessage().complete(), event.getUser());
            }
        }
    }


    public void queue(Message message, Emote emote, ReactionOperator op){
        message.addReaction(emote).queue();
        ReactionUnit unit = new ReactionUnit(message, emote, op);
        queue.add(unit);
    }

    public void queue(Message message, MessageReaction.ReactionEmote emote, ReactionOperator op){
        message.addReaction(emote.getEmoji()).queue();
        ReactionUnit unit = new ReactionUnit(message, emote, op);
        queue.add(unit);
    }

    public void clearQueueForMessageID(Long messageID){
        queue.removeIf(unit -> unit.getMessage().getIdLong() == messageID);
    }
}
