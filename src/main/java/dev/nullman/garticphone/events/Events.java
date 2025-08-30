package dev.nullman.garticphone.events;

import dev.nullman.garticphone.Garticphone;
import dev.nullman.garticphone.objects.ArenaObject;
import dev.nullman.garticphone.objects.GameObject;
import dev.nullman.garticphone.types.GameRound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class Events implements Listener {

    public Events() {
        Garticphone.getInstance().getLogger().info("Events loaded.");
    }

    @EventHandler
    public void asyncChatSend(AsyncPlayerChatEvent event) {
        UUID connectedArena = GameObject.playerLinksArena.get(event.getPlayer().getUniqueId());
        System.out.println("Player " + event.getPlayer().getName() + " sent a message in arena " + connectedArena);
        if(connectedArena == null) return;
        System.out.println("Current round: " + Garticphone.getInstance().getGameObject().getCurrentRound());
        if(!Garticphone.getInstance().getGameObject().getCurrentRound().equals(GameRound.SENTENCE)) return;
        System.out.println("Player " + event.getPlayer().getName() + " is in the SENTENCE round");
        event.setCancelled(true);
        Garticphone.getInstance().getServer().getScheduler().runTask(Garticphone.getInstance(), () -> {
        ArenaObject arenaObj = ArenaObject.get(connectedArena);
        System.out.println("Found arena object: " + (arenaObj != null));
        if(arenaObj == null) return;
        System.out.println("Player " + event.getPlayer().getName() + " is trying to send sentence in arena " + arenaObj.getId());
        boolean success = arenaObj.sendSentence(event.getPlayer(), event.getMessage());
        if(success) event.getPlayer().getServer().broadcastMessage(event.getPlayer().getDisplayName() + " cümlesini gönderdi.");
        });
    }

}
