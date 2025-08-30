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
        this.loc1 = centerLocation.clone().add(-32, 60, -32);
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
        System.out.println("Player " + player.getName() + " is trying to send drawing in arena " + this.id);
        if(GameObject.playerLinksArena.get(playerId) == null) return false;
        System.out.println("Player " + player.getName() + " is linked to arena " + GameObject.playerLinksArena.get(playerId));
        if(!GameObject.playerLinksArena.get(playerId).equals(this.id)) return false;
        System.out.println("Player " + player.getName() + " is in the correct arena " + this.id);
        if(turnOrder.containsValue(playerId)) return false;
        System.out.println("Player " + player.getName() + " is in the turn order of arena " + this.id);
        if(playerDrawings.containsKey(playerId)) return false;
        System.out.println("Player " + player.getName() + " has not sent a drawing yet in arena " + this.id);
        RegionSnapshot drawing = new RegionSnapshot(Garticphone.getInstance().getWorldUtil().getWorld(), loc1, loc2);
        playerDrawings.put(playerId, drawing);
        turnOrder.put(turnOrder.size() + 1, playerId);
        Garticphone.getInstance().getGameObject().readyPlayer(player);
        return true;
    }

    /**
     * Arenayı sıfırlar ama kaydedilen verileri korur
     */
    public void resetArenaKeepData() {
        // Arena çerçevesini yeniden oluştur (temiz bir arena)
        ArenaUtils.createArenaFrame(loc1, loc2, Material.BEDROCK, Material.WHITE_CONCRETE);

        // İç kısmı temizle (sadece çerçeveyi koruyarak)
        Location start = loc1.clone().add(1, 1, 1);
        Location end = loc2.clone().add(-1, -1, -1);

        // İç alanı hava blokları ile doldur
        for(int x = start.getBlockX(); x <= end.getBlockX(); x++) {
            for(int y = start.getBlockY(); y <= end.getBlockY(); y++) {
                for(int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
                    start.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Oyuncuyu arena merkezine ışınlar
     */
    public void teleportPlayerToCenter(Player player) {
        if(player == null || !player.isOnline()) return;

        Location center = loc1.clone().add(loc2).multiply(0.5);
        center.setY(60); // Güvenli bir yükseklik

        player.teleport(center);
    }

}
