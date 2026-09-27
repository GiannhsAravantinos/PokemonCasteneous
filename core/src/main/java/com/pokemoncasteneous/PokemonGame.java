package com.pokemoncasteneous;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pokemoncasteneous.screens.OverworldScreen;

public final class PokemonGame extends Game {
    private SpriteBatch batch;
    private final GameState state = new GameState();

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new OverworldScreen(this));
    }

    public SpriteBatch batch() { return batch; }
    public GameState state() { return state; }

    @Override
    public void render() {
        ScreenUtils.clear(0.10f, 0.16f, 0.18f, 1f);
        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        batch.dispose();
    }
}
