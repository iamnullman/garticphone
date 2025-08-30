package dev.nullman.garticphone.objects;

import dev.nullman.garticphone.types.GameRound;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
}
