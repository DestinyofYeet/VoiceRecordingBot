package de.uwuwhatsthis.voiceRecorderBotForClara.managers;



import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Args;
import de.uwuwhatsthis.voiceRecorderBotForClara.customObjects.Embed;
import de.uwuwhatsthis.voiceRecorderBotForClara.main.Main;
import de.uwuwhatsthis.voiceRecorderBotForClara.utils.Constants;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class CommandManager extends ListenerAdapter {
    // "main" listener event

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if (event.getChannelType().equals(ChannelType.PRIVATE) || event.getChannelType().equals(ChannelType.GROUP)){
            return;
        }

        try {
            String prefix = Main.config.getPrefix();

            if (!event.getMessage().getContentRaw().startsWith(prefix)) return;

            String commandName = event.getMessage().getContentRaw().split(prefix)[1].split(" ")[0];

            if (Constants.commandMap.containsKey(commandName)){
                commandName = Constants.commandMap.get(commandName);

            } else {
                commandName = commandName.substring(0, 1).toUpperCase() + commandName.substring(1).toLowerCase();
            }



            Args args = new Args(event.getMessage().getContentRaw());

            Class c = null;

            try {
                // finds the class in the commands folder, capitalizes the first letter and invokes the execute method in the class
                // does a bit of mapping to get the right class name even if an alias is used
                c = Class.forName("de.uwuwhatsthis.voiceRecorderBotForClara.commands." + commandName);

                Method execute = c.getDeclaredMethod("execute", MessageReceivedEvent.class, Args.class);
                Object o = c.newInstance();
                execute.invoke(o, event, args);

            } catch (ClassNotFoundException ignored){

            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
                event.getChannel().sendMessageEmbeds(new Embed("Fatal Error", "Something failed: " + e.getMessage(), Color.RED).build()).queue();
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {

        } catch (HierarchyException e){
            event.getChannel().sendMessageEmbeds(new Embed("Error", "You are trying to interact with somebody that has a higher role than the bot has!", Color.RED).build()).queue();
            // event.getChannel().sendMessage(new Embed("Error", "You are trying to interact with somebody that has a higher role than the bot has!", Color.RED).build()).queue();

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
