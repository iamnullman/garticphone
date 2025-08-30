package dev.nullman.garticphone.objects;

import dev.nullman.garticphone.types.GameRound;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import dev.nullman.garticphone.Garticphone;

import java.util.*;

@Getter
@Setter
public class GameObject {

    private GameRound currentRound = GameRound.WAITING_PLAYERS;
    public static int currentTurn = 1;
    public static HashMap<UUID, UUID> playerLinksArena = new HashMap<>();
    private List<UUID> players = new ArrayList<>();
    private List<UUID> readyPlayers = new ArrayList<>();
    private List<UUID> spectators = new ArrayList<>();

    private boolean isShowingResults = false;
    private int currentShowArenaIndex = 0;
    private int currentShowTurn = 1;
    private int currentShowStep = 0; // 0: cümle, 1: yapı
    private List<UUID> arenaShowOrder = new ArrayList<>();
    private BukkitRunnable showTask;

    public GameObject() {

    }

    public boolean startGame() {
        if(currentRound != GameRound.WAITING_PLAYERS) return false;
        if(players.size() < 3) return false;
        prepareArenaOrders();
        currentRound = GameRound.SENTENCE;
        currentTurn = 1;
        return true;
    }

    private void prepareArenaOrders() {
        List<UUID> playerList = new ArrayList<>(players);
        List<UUID> arenaList = new ArrayList<>(ArenaObject.getArenaPlayers().keySet());

        int playerCount = playerList.size();
        int arenaCount = arenaList.size();

        if (playerCount != arenaCount) {
            throw new IllegalStateException("Oyuncu sayısı ile arena sayısı eşit olmalı!");
        }

        arenaList.sort(Comparator.comparing(UUID::toString));

        for (UUID playerId : playerList) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) continue;

            ArenaObject ownArena = ArenaObject.get(playerId);
            if (ownArena == null) continue;

            int startIndex = arenaList.indexOf(ownArena.getId());
            if (startIndex == -1) continue;

            List<UUID> rotated = new ArrayList<>();
            rotated.addAll(arenaList.subList(startIndex, arenaCount));
            rotated.addAll(arenaList.subList(0, startIndex));

            for (int turn = 0; turn < arenaCount; turn++) {
                UUID arenaId = rotated.get(turn);
                ArenaObject arena = ArenaObject.get(arenaId);
                if (arena != null) {
                    arena.addTurnOrder(turn + 1, playerId);
                    Player owner = Bukkit.getPlayer(arena.getOwner());
                    String ownerName = owner != null ? owner.getDisplayName() : arena.getOwner().toString();
                    System.out.println(player.getDisplayName() + " adlı oyuncu " + (turn + 1) + ". turda " + ownerName + " arenasında.");
                }
            }
        }
    }



    private void nextRound() {
        if(currentRound == GameRound.WAITING_PLAYERS) return;
        if(currentRound == GameRound.ENDING) return;
        if(currentTurn < players.size()) {
            currentTurn++;
            teleportAllPlayersToArenas();
            if(currentRound == GameRound.SENTENCE) {
                currentRound = GameRound.BUILDING;
                broadcastMessage("§eŞimdi yapı yapma zamanı! Cümleleri yapılara dönüştürün.");
                resetArenasForBuilding();
            } else if(currentRound == GameRound.BUILDING) {
                currentRound = GameRound.SENTENCE;
                broadcastMessage("§eŞimdi cümle zamanı! Yapıları cümlelere dönüştürün.");
            }
        } else {
            broadcastMessage("§aTüm turlar tamamlandı! Oyun sona erdi.");
            broadcastMessage("§eOyun sonuçlarını görmek için: §6/garticphone goster");
            currentRound = GameRound.ENDING;
        }
    }

    private void broadcastMessage(String message) {
        for(UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if(player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }

    private void broadcastTitle(String title, String subtitle) {
        for(UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if(player != null && player.isOnline()) {
                player.sendTitle(title, subtitle, 10, 70, 20);
            }
        }
    }

    private void resetArenasForBuilding() {
        for(UUID playerId : players) {
            UUID arenaId = playerLinksArena.get(playerId);
            if(arenaId != null) {
                ArenaObject arena = ArenaObject.get(arenaId);
                if(arena != null) {
                    String sentence = arena.getPlayerSentence(arena.getPlayerAtTurn(currentTurn - 1));
                    sendTitleToPlayer(playerId, sentence);
                    Player player = Bukkit.getPlayer(playerId);
                    arena.resetArena();
                    arena.teleportPlayerToCenter(player);
                    player.sendMessage("§eCümleniz: §a" + sentence);
                }
            }
        }
    }

    private void teleportAllPlayersToArenas() {
        List<ArenaObject> arenas = new ArrayList<>(ArenaObject.getArenas());
        for (ArenaObject arena: arenas) {
            UUID playerId = arena.getPlayerAtTurn(currentTurn);
            if (playerId != null) {
                playerLinksArena.put(playerId, arena.getId());
                teleportPlayerToArena(playerId, arena.getId());
            }
        }
    }

    private void sendTitleToPlayer(UUID playerId, String sentence) {
        Player player = Bukkit.getPlayer(playerId);
        if(player != null && player.isOnline()) {
            player.sendTitle("§6Çizim Konusu:", "§e" + sentence, 10, 100, 20);
        }
    }

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
            System.out.println("All players are ready. Moving to next round.");
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

    public boolean startShowResults() {
        if (isShowingResults) {
            return false;
        }

        if (currentRound != GameRound.ENDING) {
            return false;
        }

        isShowingResults = true;
        currentShowArenaIndex = 0;
        currentShowTurn = 1;
        currentShowStep = 0;

        // Arena sırasını belirle
        arenaShowOrder.clear();
        arenaShowOrder.addAll(ArenaObject.getArenaPlayers().keySet());
        arenaShowOrder.sort(Comparator.comparing(UUID::toString)); // Sabit sıralama için

        broadcastMessage("§6§l=== OYUN SONUÇLARI GÖSTERİMİ BAŞLIYOR ===");
        broadcastMessage("§eHer arenada: Cümle → Yapı → Yanıt → Yapı → ... şeklinde devam edilecek");

        // İlk gösterimi başlat
        showNextStep();
        return true;
    }

    private void showNextStep() {
        if (currentShowArenaIndex >= arenaShowOrder.size()) {
            // Tüm arenalar bitti
            endShowResults();
            return;
        }

        UUID currentArenaId = arenaShowOrder.get(currentShowArenaIndex);
        ArenaObject arena = ArenaObject.get(currentArenaId);

        if (arena == null) {
            nextArena();
            return;
        }

        // Bu arenada gösterilecek bir şey kaldı mı kontrol et
        if (currentShowTurn > players.size()) {
            nextArena();
            return;
        }

        // Arena sahibini bul
        Player arenaOwner = Bukkit.getPlayer(arena.getOwner());
        String ownerName = arenaOwner != null ? arenaOwner.getDisplayName() : "Bilinmeyen Oyuncu";

        if (currentShowStep == 0) {
            // Cümle göster
            showSentenceStep(arena, ownerName);
        } else {
            // Yapı göster
            showBuildingStep(arena, ownerName);
        }
    }

    private void showSentenceStep(ArenaObject arena, String ownerName) {
        // Tüm oyuncuları bu arenaya ışınla
        teleportAllPlayersToArena(arena);

        // Arena'yı temizle
        arena.resetArena();

        // Bu turda kim cümle yazdı?
        UUID writerId = arena.getPlayerAtTurn(currentShowTurn);
        if (writerId == null) {
            nextStep();
            return;
        }

        String sentence = arena.getPlayerSentence(writerId);
        if (sentence == null) {
            nextStep();
            return;
        }

        Player writer = Bukkit.getPlayer(writerId);
        String writerName = writer != null ? writer.getDisplayName() : "Bilinmeyen";

        // Mesajları gönder
        broadcastMessage("§6§l=== " + ownerName + " ARENASI ===");
        broadcastMessage("§eTur " + currentShowTurn + " - " + writerName + " tarafından yazılan cümle:");
        broadcastMessage("§f\"" + sentence + "\"");

        // Title gönder
        broadcastTitle("§6" + ownerName + " Arenası", "§e" + writerName + " - Cümle");

        // Action bar
        sendActionBarToAll("§6Cümle: §e" + sentence);

        // 4 saniye sonra yapıyı göster
        scheduleNext(4);
    }

    private void showBuildingStep(ArenaObject arena, String ownerName) {
        // Bu turda kim yapı yaptı?
        UUID builderId = arena.getPlayerAtTurn(currentShowTurn);
        if (builderId == null) {
            nextStep();
            return;
        }

        RegionSnapshot building = arena.getPlayerDrawing(builderId);
        if (building == null) {
            nextStep();
            return;
        }

        Player builder = Bukkit.getPlayer(builderId);
        String builderName = builder != null ? builder.getDisplayName() : "Bilinmeyen";

        // Arena'yı temizle ve yapıyı restore et
        arena.resetArena();
        building.restore();

        // Mesajları gönder
        broadcastMessage("§aTur " + currentShowTurn + " - " + builderName + " tarafından yapılan yapı:");

        // Title gönder
        broadcastTitle("§6" + ownerName + " Arenası", "§a" + builderName + " - Yapı");

        // Action bar
        sendActionBarToAll("§aYapı: §e" + builderName + " §atarafından yapıldı");

        // 5 saniye sonra bir sonraki adıma geç
        scheduleNext(5);
    }

    private void teleportAllPlayersToArena(ArenaObject arena) {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                arena.teleportPlayerToCenter(player);
            }
        }
    }

    private void scheduleNext(int seconds) {
        if (showTask != null) {
            showTask.cancel();
        }

        showTask = new BukkitRunnable() {
            @Override
            public void run() {
                nextStep();
            }
        };
        showTask.runTaskLater(Garticphone.getInstance(), seconds * 20L);
    }

    private void nextStep() {
        if (currentShowStep == 0) {
            // Cümleden yapıya geç
            currentShowStep = 1;
        } else {
            // Yapıdan bir sonraki tura geç
            currentShowStep = 0;
            currentShowTurn++;
        }

        showNextStep();
    }

    private void nextArena() {
        currentShowArenaIndex++;
        currentShowTurn = 1;
        currentShowStep = 0;
        showNextStep();
    }

    private void endShowResults() {
        isShowingResults = false;

        if (showTask != null) {
            showTask.cancel();
            showTask = null;
        }

        broadcastMessage("§6§l=== TÜM ARENALAR GÖSTERİLDİ ===");
        broadcastMessage("§aTebrikler! Tüm oyun sonuçları gösterildi.");
        broadcastTitle("§6Gösterim Tamamlandı!", "§aTebrikler!");

        // Oyuncuları spawn'a ışınla
        teleportPlayersToSpawn();
    }

    private void teleportPlayersToSpawn() {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.teleport(player.getWorld().getSpawnLocation());
            }
        }
    }

    private void sendActionBarToAll(String message) {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(message));
            }
        }
    }
}
