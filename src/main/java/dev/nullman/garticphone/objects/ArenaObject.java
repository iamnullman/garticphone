package dev.nullman.garticphone.objects;

import dev.nullman.garticphone.Garticphone;
import dev.nullman.garticphone.utils.ArenaUtils;
import dev.nullman.garticphone.utils.WorldUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

@Getter
@Setter
public class ArenaObject {
    private static final HashMap<UUID, ArenaObject> map = new HashMap<>();

    public static ArenaObject get(UUID id) {
        return map.computeIfAbsent(id, ArenaObject::new);
    }

    private UUID id;
    private HashMap<UUID, String> playerSentences = new HashMap<>();
    private HashMap<UUID, RegionSnapshot> playerDrawings = new HashMap<>();
    private HashMap<Integer, UUID> turnOrder = new HashMap<>();
    private HashSet<UUID> players = new HashSet<>();
    private UUID owner;
    private Location loc1;
    private Location loc2;

    private ArenaObject(UUID id) {
        this.id = id;
        this.owner = id;
        this.players.add(id);
        Location centerLocation = Garticphone.getInstance().getWorldUtil().getNextEmptyChunk();
        this.loc1 = centerLocation.clone().add(-32, 0, -32);
        this.loc2 = centerLocation.clone().add(32, 120, 32);
        ArenaUtils.createArenaFrame(loc1, loc2, Material.BEDROCK, Material.WHITE_CONCRETE);
        Location center = loc1.clone().add(loc2).multiply(0.5);
        center.setY(60);
        Player ownerPlayer = Garticphone.getInstance().getServer().getPlayer(id);
        if(ownerPlayer != null) ownerPlayer.teleport(center);
        GameObject.playerLinksArena.put(id, id);
    }

    public boolean sendSentence(Player player, String sentence) {
        UUID playerId = player.getUniqueId();
        if(GameObject.playerLinksArena.get(playerId) == null) return false;
        if(!GameObject.playerLinksArena.get(playerId).equals(this.id)) return false;
        if(turnOrder.containsValue(playerId)) return false;
        if(playerSentences.containsKey(playerId)) return false;
        playerSentences.put(playerId, sentence);
        turnOrder.put(turnOrder.size() + 1, playerId);
        Garticphone.getInstance().getGameObject().readyPlayer(player);
        return true;
    }

    public boolean sendDrawing(Player player) {
        UUID playerId = player.getUniqueId();
        if(GameObject.playerLinksArena.get(playerId) == null) return false;
        if(!GameObject.playerLinksArena.get(playerId).equals(this.id)) return false;
        if(!turnOrder.containsValue(playerId)) return false;
        if(playerDrawings.containsKey(playerId)) return false;
        RegionSnapshot drawing = new RegionSnapshot(Garticphone.getInstance().getWorldUtil().getWorld(), loc1, loc2);
        playerDrawings.put(playerId, drawing);
        turnOrder.put(turnOrder.size() + 1, playerId);
        Garticphone.getInstance().getGameObject().readyPlayer(player);
        return true;
    }

}
