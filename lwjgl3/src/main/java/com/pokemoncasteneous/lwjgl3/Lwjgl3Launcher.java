package com.pokemoncasteneous.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.pokemoncasteneous.PokemonGame;

public final class Lwjgl3Launcher {
    private Lwjgl3Launcher() { }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Fernwood - A Tiny Creature RPG");
        config.setWindowedMode(1100, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new PokemonGame(), config);
    }
}
