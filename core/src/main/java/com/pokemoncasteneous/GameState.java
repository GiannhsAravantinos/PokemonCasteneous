package com.pokemoncasteneous;

import com.pokemoncasteneous.overworld.PlayerPosition;
import com.pokemoncasteneous.player.PlayerInfo;

import static com.pokemoncasteneous.constants.GameplayConstants.*;

public final class GameState {
    public final PlayerInfo playerInfo = PlayerInfo.defaultPlayer();

    // Position in grid-square units; whole numbers mark the center of a square.
    public PlayerPosition playerPosition = new PlayerPosition(0f, 0f);
    public boolean overworldPositionInitialized;
    public boolean playerInfoVisible;

    // These values change during play; their starting values and limits live in GameplayConstants.
    public int playerHp = PLAYER_MAX_HP;
    public int enemyHp = ENEMY_MAX_HP;
    public int potions = STARTING_POTIONS;
    public int partyCount = STARTING_PARTY_SIZE;
    public String message = "A wild Mossling appeared!";

    public void beginEncounter() {
        enemyHp = ENEMY_MAX_HP;
        message = "A wild Mossling appeared!";
    }

    public void returnToOverworld() {
        playerHp = PLAYER_MAX_HP;
        beginEncounter();
    }
}
