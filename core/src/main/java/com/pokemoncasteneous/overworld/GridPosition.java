package com.pokemoncasteneous.overworld;

public record GridPosition(int x, int y) {
    public GridPosition translate(MovementInput movement) {
        return new GridPosition(x + movement.dx(), y + movement.dy());
    }
}
