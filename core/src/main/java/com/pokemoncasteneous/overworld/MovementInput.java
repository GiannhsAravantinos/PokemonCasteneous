package com.pokemoncasteneous.overworld;

public record MovementInput(int dx, int dy) {
    public static final MovementInput NONE = new MovementInput(0, 0);

    public MovementInput {
        dx = Integer.compare(dx, 0);
        dy = Integer.compare(dy, 0);
        if (dx != 0 && dy != 0) dx = 0;
    }

    public boolean isMoving() {
        return dx != 0 || dy != 0;
    }

    public Facing facing() {
        if (dx < 0) return Facing.LEFT;
        if (dx > 0) return Facing.RIGHT;
        if (dy > 0) return Facing.UP;
        if (dy < 0) return Facing.DOWN;
        return null;
    }
}
