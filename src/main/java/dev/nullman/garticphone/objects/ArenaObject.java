package dev.nullman.garticphone.objects;

import dev.nullman.garticphone.Garticphone;
import dev.nullman.garticphone.utils.ArenaUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private RegionSnapshot originalSnapshot;

    private ArenaObject(UUID id) {
        this.id = id;
        this.owner = id;
        this.players.add(id);
        this.turnOrder.put(1, id);
        Location centerLocation = Garticphone.getInstance().getWorldUtil().getNextEmptyChunk();
        this.loc1 = centerLocation.clone().add(-32, 60, -32);
        this.loc2 = centerLocation.clone().add(32, 120, 32);
        ArenaUtils.createArenaFrame(loc1, loc2, Material.BEDROCK, Material.WHITE_CONCRETE);
        Location center = loc1.clone().add(loc2).multiply(0.5);
        center.setY(60);
        this.originalSnapshot = new RegionSnapshot(Garticphone.getInstance().getWorldUtil().getWorld(), loc1, loc2);
        Player ownerPlayer = Garticphone.getInstance().getServer().getPlayer(id);
        if(ownerPlayer != null) ownerPlayer.teleport(center);
        GameObject.playerLinksArena.put(id, id);
    }

    public void addPlayer(UUID playerID) {
        this.players.add(playerID);
        GameObject.playerLinksArena.put(playerID, this.id);
        Player player = Garticphone.getInstance().getServer().getPlayer(playerID);
    }

    public void addTurnOrder(int slot, UUID playerID) {
        this.turnOrder.put(slot, playerID);
    }

    public String getPlayerSentence(UUID playerID) {
        return playerSentences.get(playerID);
    }

    public void resetArena() {
        if(originalSnapshot != null) {
            originalSnapshot.restore();
        }
    }

    public RegionSnapshot getPlayerDrawing(UUID playerID) {
        return playerDrawings.get(playerID);
    }

    public UUID getPlayerAtTurn(int turn) {
        return turnOrder.get(turn);
    }

    public boolean sendSentence(Player player, String sentence) {
        UUID playerId = player.getUniqueId();
        System.out.println("1");
        if(GameObject.playerLinksArena.get(playerId) == null) return false;
        System.out.println("2");
        if(!GameObject.playerLinksArena.get(playerId).equals(this.id)) return false;
        System.out.println("3");
        if(!turnOrder.containsValue(playerId)) return false;
        System.out.println("4");
        if(!turnOrder.get(GameObject.currentTurn).equals(playerId)) return false;
        System.out.println("5");
        if(playerSentences.containsKey(playerId)) return false;
        System.out.println("6");
        playerSentences.put(playerId, sentence);
        Garticphone.getInstance().getGameObject().readyPlayer(player);
        return true;
    }

    public boolean sendDrawing(Player player) {
        UUID playerId = player.getUniqueId();
        System.out.println("1");
        if(GameObject.playerLinksArena.get(playerId) == null) return false;
        System.out.println("2");
        if(!GameObject.playerLinksArena.get(playerId).equals(this.id)) return false;
        System.out.println("3");
        if(!turnOrder.containsValue(playerId)) return false;
        System.out.println("4");
        if(!turnOrder.get(GameObject.currentTurn).equals(playerId)) return false;
        System.out.println("5");
        if(playerDrawings.containsKey(playerId)) return false;
        System.out.println("6");
        RegionSnapshot drawing = new RegionSnapshot(Garticphone.getInstance().getWorldUtil().getWorld(), loc1, loc2);
        playerDrawings.put(playerId, drawing);
        turnOrder.put(turnOrder.size() + 1, playerId);
        Garticphone.getInstance().getGameObject().readyPlayer(player);
        return true;
    }

    public void resetArenaKeepData() {
        ArenaUtils.createArenaFrame(loc1, loc2, Material.BEDROCK, Material.WHITE_CONCRETE);

        Location start = loc1.clone().add(1, 1, 1);
        Location end = loc2.clone().add(-1, -1, -1);

        for(int x = start.getBlockX(); x <= end.getBlockX(); x++) {
            for(int y = start.getBlockY(); y <= end.getBlockY(); y++) {
                for(int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
                    start.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public void teleportPlayerToCenter(Player player) {
        if(player == null || !player.isOnline()) return;

        Location center = loc1.clone().add(loc2).multiply(0.5);
        center.setY(60);

        player.teleport(center);
    }


    public static List<ArenaObject> getArenas() {
        return map.values().stream().toList();
    }

    public static HashMap<UUID, List<UUID>> getArenaPlayers() {
        HashMap<UUID, List<UUID>> arenaPlayers = new HashMap<>();
        for(ArenaObject arena : map.values()) {
            arenaPlayers.put(arena.getId(), arena.getPlayers().stream().toList());
        }
        return arenaPlayers;
    }
}
