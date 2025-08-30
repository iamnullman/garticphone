package dev.nullman.garticphone.commands;

import dev.nullman.garticphone.Garticphone;
import dev.nullman.garticphone.objects.ArenaObject;
import dev.nullman.garticphone.objects.GameObject;
import dev.nullman.garticphone.types.GameRound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.UUID;

@Command("garticphone")
public class Commands {

    @Subcommand("start")
    @CommandPermission("garticphone.start")
    public void startGame(BukkitCommandActor player) {
        Player p = player.requirePlayer();
        boolean isStart = Garticphone.getInstance().getGameObject().startGame();
        if(isStart) {
            p.sendMessage(ChatColor.GREEN + "ᴏʏᴜɴ ʙᴀşʟᴀᴅɪ");
        } else {
            p.sendMessage(ChatColor.RED + "ᴏʏᴜɴ ʙᴀşʟᴀᴍᴀᴅɪ");
        }
    }

    @Subcommand("join")
    @CommandPermission("garticphone.join")
    public void joinGame(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        boolean isJoin = Garticphone.getInstance().getGameObject().joinGame(player);
        if(isJoin) {
            player.sendMessage(ChatColor.WHITE + player.getDisplayName() + ChatColor.GREEN + " ᴏʏᴜɴᴀ ᴋᴀᴛɪʟᴅɪ");
        } else {
            player.sendMessage(ChatColor.RED+"ᴏʏᴜɴᴀ ᴋᴀᴛɪʟᴀᴍᴀᴢѕɪɴɪᴢ");
        }
    }

    @Subcommand("ready")
    @CommandPermission("garticphone.ready")
    public void readyGame(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        GameRound currentRound = Garticphone.getInstance().getGameObject().getCurrentRound();
        if(!currentRound.equals(GameRound.BUILDING)) return;
        UUID arenaID = GameObject.playerLinksArena.get(player.getUniqueId());
        if(arenaID == null) return;
        ArenaObject arenaObj = ArenaObject.get(arenaID);
        if(arenaObj == null) return;
        boolean isReady = arenaObj.sendDrawing(player);
        if(isReady) {
            player.sendMessage(ChatColor.GREEN + "ʏᴀᴘıɴıᴢ ᴋᴀʏᴅᴇᴅɪʟᴅɪ.");
        } else {
            player.sendMessage(ChatColor.RED + "ʏᴀᴘıɴıᴢ ᴋᴀʏᴅᴇᴅɪʟᴍᴇʟɪ.");
        }
    }
}
