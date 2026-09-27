package com.pokemoncasteneous;

import static com.pokemoncasteneous.cosntants.GameConstants.*;

public final class GameState {
    // Position in the overworld, measured in world pixels.
    public float playerX = 0f;
    public float playerY = 0f;

    // These values change during play; their starting values and limits live in GameConstants.
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
