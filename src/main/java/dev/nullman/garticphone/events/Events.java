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
        if(connectedArena == null) return;
        if(!Garticphone.getInstance().getGameObject().getCurrentRound().equals(GameRound.SENTENCE)) return;
        Garticphone.getInstance().getServer().getScheduler().runTask(Garticphone.getInstance(), () -> {
        ArenaObject arenaObj = ArenaObject.get(connectedArena);
        if(arenaObj == null) return;
        boolean success = arenaObj.sendSentence(event.getPlayer(), event.getMessage());
        if(success) event.getPlayer().sendMessage("§aᴄüᴍʟᴇɴɪᴢɪ ᴋᴀʏᴅᴇᴛᴛɪᴍ.");
        if(success) event.setCancelled(true);
        });
    }

}
