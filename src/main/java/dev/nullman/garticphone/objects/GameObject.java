package dev.nullman.garticphone.objects;

import dev.nullman.garticphone.types.GameRound;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GameObject {

    private GameRound currentRound = GameRound.WAITING_PLAYERS;
    private int currentTurn = 0;
    public static HashMap<UUID, UUID> playerLinksArena = new HashMap<>();
    private List<UUID> players;
    private List<UUID> readyPlayers;
    private List<UUID> spectators;

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

            } else if(currentRound == GameRound.BUILDING) {
                currentRound = GameRound.SENTENCE;
            }
        } else {
            // TODO: end game and show results
            currentRound = GameRound.ENDING;
        }
        return true;
    }

    public void readyPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        if(currentRound != GameRound.WAITING_PLAYERS) return;
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
