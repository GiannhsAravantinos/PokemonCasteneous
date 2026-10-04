package com.pokemoncasteneous.assets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public final class Tile {
    private final int id;
    private final String image;
    private final boolean overworld;
    private final boolean foreground;
    private final boolean traversable;
    private final int underlayId;
}
