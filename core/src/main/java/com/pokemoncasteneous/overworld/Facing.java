package com.pokemoncasteneous.overworld;

public enum Facing {
    DOWN(0),
    LEFT(1),
    RIGHT(2),
    UP(3);

    private final int spriteRow;

    Facing(int spriteRow) {
        this.spriteRow = spriteRow;
    }

    public int spriteRow() {
        return spriteRow;
    }
}
