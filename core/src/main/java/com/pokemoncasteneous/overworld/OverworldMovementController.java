package com.pokemoncasteneous.overworld;

import com.pokemoncasteneous.GameState;
import com.pokemoncasteneous.assets.OverworldMap;
import com.pokemoncasteneous.constants.GameplayConstants;

public final class OverworldMovementController {
    private final GameState gameState;
    private final OverworldMap overworldMap;
    private PlayerPosition startPosition;
    private PlayerPosition targetPosition;
    private float stepProgress;
    private Facing facing = Facing.DOWN;
    private boolean moving;

    public OverworldMovementController(GameState gameState, OverworldMap overworldMap) {
        this.gameState = gameState;
        this.overworldMap = overworldMap;
    }

    public void updateMovementFunctionality(float delta, MovementInput input) {
        Facing inputFacing = input.facing();
        if (inputFacing != null) {
            facing = inputFacing;
        }

        float timeLeft = delta;
        while (timeLeft > 0f && (moving || input.isMoving())) {
            if (!moving && !beginStep(input)) {
                // No step starts when the target tile is blocked or outside the map.
                break;
            }

            float timeForStep = (1f - stepProgress) / GameplayConstants.PLAYER_MOVE_SPEED;
            float stepTime = Math.min(timeLeft, timeForStep);
            stepProgress = Math.min(1f, stepProgress + stepTime * GameplayConstants.PLAYER_MOVE_SPEED);
            timeLeft -= stepTime;
            gameState.playerPosition = startPosition.interpolate(targetPosition, stepProgress);

            if (stepProgress >= 1f) {
                gameState.playerPosition = targetPosition;
                moving = false;
                stepProgress = 0f;
                if (!input.isMoving()) {
                    // Stop after landing when the player is no longer pressing a direction.
                    break;
                }
            }
        }
    }

    public Facing facing() {
        return facing;
    }

    public boolean isMoving() {
        return moving;
    }

    private boolean beginStep(MovementInput input) {
        GridPosition start = gameState.playerPosition.roundedGridPosition();
        GridPosition target = start.translate(input);
        if (!overworldMap.isTraversable(target)) {
            return false;
        }

        startPosition = PlayerPosition.at(start);
        targetPosition = PlayerPosition.at(target);
        stepProgress = 0f;
        moving = true;
        return true;
    }
}
