package dev.nullman.garticphone.objects;

import dev.nullman.garticphone.types.GameRound;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
@Setter
public class GameObject {

    private GameRound currentRound = GameRound.WAITING_PLAYERS;
    private int currentTurn = 0;
    public static HashMap<UUID, UUID> playerLinksArena = new HashMap<>();
    private List<UUID> players = new ArrayList<>();
    private List<UUID> readyPlayers = new ArrayList<>();
    private List<UUID> spectators = new ArrayList<>();

    public GameObject() {

    }

    public boolean startGame() {
        if(currentRound != GameRound.WAITING_PLAYERS) return false;
        if(players.size() < 3) return false;
        currentRound = GameRound.SENTENCE;
        currentTurn = 1;
        return true;
    }

    private boolean nextRound() {
        if(currentRound == GameRound.WAITING_PLAYERS) return false;
        if(currentRound == GameRound.ENDING) return false;
        if(currentTurn < players.size()) {
            currentTurn++;
            if(currentRound == GameRound.SENTENCE) {
                currentRound = GameRound.BUILDING;
                redistributePlayersRandomly();
                resetArenasForBuilding();
            } else if(currentRound == GameRound.BUILDING) {
                currentRound = GameRound.SENTENCE;
                redistributePlayersRandomly();
            }
        } else {
            // TODO: end game and show results
            currentRound = GameRound.ENDING;
        }
        return true;
    }

    /**
     * Oyuncuları rastgele arenalara dağıtır
     */
    private void redistributePlayersRandomly() {
        if(players == null || players.isEmpty()) return;

        List<UUID> arenaIds = new ArrayList<>(playerLinksArena.values());
        arenaIds = new ArrayList<>(new HashSet<>(arenaIds));

        if(arenaIds.isEmpty()) return;

        Collections.shuffle(players);
        Collections.shuffle(arenaIds);

        // Her oyuncu için uygun arena bul
        for(UUID playerId : players) {
            UUID assignedArena = null;

            // Önce oyuncunun daha önce işlem yapmadığı arenaları bul
            List<UUID> availableArenas = new ArrayList<>();
            for(UUID arenaId : arenaIds) {
                if(!hasPlayerWorkedInArena(playerId, arenaId)) {
                    availableArenas.add(arenaId);
                }
            }

            // Eğer hiç uygun arena yoksa, en az işlem yapılan arenayı seç
            if(availableArenas.isEmpty()) {
                assignedArena = findLeastWorkedArena(playerId, arenaIds);
            } else {
                // Rastgele uygun arenayı seç
                Collections.shuffle(availableArenas);
                assignedArena = availableArenas.get(0);
            }

            if(assignedArena != null) {
                playerLinksArena.put(playerId, assignedArena);
                teleportPlayerToArena(playerId, assignedArena);
            }
        }
    }

    /**
     * Oyuncunun belirtilen arenada daha önce işlem yapıp yapmadığını kontrol eder
     */
    private boolean hasPlayerWorkedInArena(UUID playerId, UUID arenaId) {
        ArenaObject arena = ArenaObject.get(arenaId);
        if(arena == null) return false;

        // Sentence yazmış mı kontrol et
        if(arena.getPlayerSentences().containsKey(playerId)) {
            return true;
        }

        // Drawing yapmış mı kontrol et
        if(arena.getPlayerDrawings().containsKey(playerId)) {
            return true;
        }

        return false;
    }

    /**
     * Oyuncunun en az işlem yaptığı arenayı bulur (son çare)
     */
    private UUID findLeastWorkedArena(UUID playerId, List<UUID> arenaIds) {
        UUID bestArena = null;
        int minWorkCount = Integer.MAX_VALUE;

        for(UUID arenaId : arenaIds) {
            ArenaObject arena = ArenaObject.get(arenaId);
            if(arena == null) continue;

            int workCount = 0;
            if(arena.getPlayerSentences().containsKey(playerId)) workCount++;
            if(arena.getPlayerDrawings().containsKey(playerId)) workCount++;

            if(workCount < minWorkCount) {
                minWorkCount = workCount;
                bestArena = arenaId;
            }
        }

        return bestArena;
    }

    /**
     * Build aşaması için arenaları sıfırlar ve önceki sentence'ları title olarak gönderir
     */
    private void resetArenasForBuilding() {
        for(UUID arenaId : new HashSet<>(playerLinksArena.values())) {
            ArenaObject arena = ArenaObject.get(arenaId);
            if(arena != null) {
                // Önceki tur sentence'ını al
                String previousSentence = getPreviousPlayerSentence(arenaId);

                // Arenayı sıfırla ama kaydedilenleri koru
                arena.resetArenaKeepData();

                // Bu arenada olan oyuncuya title gönder
                UUID currentPlayerInArena = getCurrentPlayerInArena(arenaId);
                if(currentPlayerInArena != null && previousSentence != null) {
                    sendTitleToPlayer(currentPlayerInArena, previousSentence);
                }
            }
        }
    }

    /**
     * Belirli arenada şu anda bulunan oyuncuyu bulur
     */
    private UUID getCurrentPlayerInArena(UUID arenaId) {
        for(Map.Entry<UUID, UUID> entry : playerLinksArena.entrySet()) {
            if(entry.getValue().equals(arenaId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Önceki oyuncunun sentence'ını getirir
     */
    private String getPreviousPlayerSentence(UUID arenaId) {
        ArenaObject arena = ArenaObject.get(arenaId);
        if(arena != null && !arena.getPlayerSentences().isEmpty()) {
            // Son eklenen sentence'ı al
            return arena.getPlayerSentences().values().iterator().next();
        }
        return null;
    }

    /**
     * Oyuncuya title gönderir
     */
    private void sendTitleToPlayer(UUID playerId, String sentence) {
        Player player = Bukkit.getPlayer(playerId);
        if(player != null && player.isOnline()) {
            player.sendTitle("§6Çizim Konusu:", "§e" + sentence, 10, 100, 20);
        }
    }

    /**
     * Oyuncuyu belirtilen arenaya ışınlar
     */
    private void teleportPlayerToArena(UUID playerId, UUID arenaId) {
        Player player = Bukkit.getPlayer(playerId);
        if(player != null && player.isOnline()) {
            ArenaObject arena = ArenaObject.get(arenaId);
            if(arena != null) {
                arena.teleportPlayerToCenter(player);
            }
        }
    }

    public void readyPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        if(currentRound == GameRound.WAITING_PLAYERS || currentRound == GameRound.ENDING) return;
        if(!players.contains(playerId)) return;
        if(readyPlayers.contains(playerId)) return;
        readyPlayers.add(playerId);
        if(readyPlayers.size() == players.size()) {
            nextRound();
            readyPlayers.clear();
        }
    }

    public boolean joinGame(Player player) {
        UUID playerId = player.getUniqueId();
        if(currentRound != GameRound.WAITING_PLAYERS) return false;
        if(players.contains(playerId)) return false;
        players.add(playerId);
        ArenaObject arena = ArenaObject.get(playerId);
        playerLinksArena.put(playerId, arena.getId());
        return true;
    }

    public boolean leaveGame(Player player) {
        UUID playerId = player.getUniqueId();
        if(currentRound != GameRound.WAITING_PLAYERS) return false;
        if(!players.contains(playerId)) return false;
        players.remove(playerId);
        playerLinksArena.remove(playerId);
        return true;
    }
}
