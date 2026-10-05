package com.pokemoncasteneous.player;

import java.util.concurrent.ThreadLocalRandom;

public record PlayerInfo(int id, String name, Gender gender, int age) {
    public static PlayerInfo defaultPlayer() {
        return new PlayerInfo(ThreadLocalRandom.current().nextInt(10_001), "Gabe Itc", Gender.MAN, 15);
    }

    public enum Gender {
        MAN("man"),
        WASHING_MACHINE("washing machine");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}
