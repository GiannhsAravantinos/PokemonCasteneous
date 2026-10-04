package com.pokemoncasteneous.overworld;

public record PlayerPosition(float x, float y) {
    public static PlayerPosition at(GridPosition position) {
        return new PlayerPosition(position.x(), position.y());
    }

    public GridPosition roundedGridPosition() {
        return new GridPosition(Math.round(x), Math.round(y));
    }

    public PlayerPosition interpolate(PlayerPosition target, float progress) {
        return new PlayerPosition(
                x + (target.x() - x) * progress,
                y + (target.y() - y) * progress);
    }
}
