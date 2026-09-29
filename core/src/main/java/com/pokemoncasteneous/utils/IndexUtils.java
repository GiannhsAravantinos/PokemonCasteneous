package com.pokemoncasteneous.utils;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class IndexUtils {
    private IndexUtils() { }

    public static Stream<Index> indices(int count) {
        if (count < 0) throw new IllegalArgumentException("Index count cannot be negative");
        return IntStream.range(0, count).mapToObj(Index::new);
    }

    public static Stream<CartesianIndex> cartesianIndices(int width, int height) {
        return cartesianIndices(0, width - 1, 0, height - 1);
    }

    public static Stream<CartesianIndex> cartesianIndices(int firstX, int lastX, int firstY, int lastY) {
        if (firstX > lastX || firstY > lastY) return Stream.empty();
        return IntStream.rangeClosed(firstY, lastY).boxed()
                .flatMap(y -> IntStream.rangeClosed(firstX, lastX)
                        .mapToObj(x -> new CartesianIndex(x, y)));
    }

    public record Index(int value) { }
    public record CartesianIndex(int x, int y) { }
}
