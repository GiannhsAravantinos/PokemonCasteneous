package com.pokemoncasteneous;

public final class GameState {
    public float playerX = 0f;
    public float playerY = 0f;
    public int playerHp = 36;
    public final int playerMaxHp = 36;
    public int enemyHp = 28;
    public final int enemyMaxHp = 28;
    public int potions = 3;
    public int partyCount = 3;
    public String message = "A wild Mossling appeared!";

    public void beginEncounter() {
        enemyHp = enemyMaxHp;
        message = "A wild Mossling appeared!";
    }

    public void returnToOverworld() {
        playerHp = playerMaxHp;
        beginEncounter();
    }
}
