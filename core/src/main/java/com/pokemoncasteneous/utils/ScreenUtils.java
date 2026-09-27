package com.pokemoncasteneous.utils;

import com.badlogic.gdx.graphics.Color;

public final class ScreenUtils {
    private ScreenUtils() { }

    public static void clear(Color color) {
        com.badlogic.gdx.utils.ScreenUtils.clear(color.r, color.g, color.b, color.a);
    }
}
