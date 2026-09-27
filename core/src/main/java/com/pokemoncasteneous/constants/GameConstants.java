package com.pokemoncasteneous.constants;

public final class GameConstants {
    // The overworld always shows this many logical squares, independent of window pixels.
    public static final int VIEW_COLUMNS = 32;
    public static final int VIEW_ROWS = 18;
    // Movement speed is measured in grid squares per second.
    public static final float PLAYER_MOVE_SPEED = 3f;
    public static final float PLAYER_SPRITE_WIDTH = 1f;
    public static final float PLAYER_SPRITE_HEIGHT = 1f;
    public static final int PLAYER_MAX_HP = 36;
    public static final int ENEMY_MAX_HP = 28;
    public static final int STARTING_POTIONS = 3;
    public static final int STARTING_PARTY_SIZE = 3;

    public static final int QUICK_PAW_DAMAGE = 6;
    public static final int ENEMY_COUNTER_DAMAGE = 5;
    public static final int POTION_HEAL_AMOUNT = 14;
    public static final int BENCH_HEAL_AMOUNT = 4;
    public static final int BENCH_ATTACK_DAMAGE = 3;
    public static final int LEAF_LASH_DAMAGE = 9;

    private GameConstants() { }
}
