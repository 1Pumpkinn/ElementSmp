package net.saturn.elementSmp.config;

public final class Constants {
    private Constants() {}

    public static final class Timing {
        public static final long TICKS_PER_SECOND = 20L;
        public static final long HALF_SECOND = 10L;
        public static final long TWO_SECONDS = 40L;
        public static final long FIVE_SECONDS = 100L;
        public static final long TEN_SECONDS = 200L;
        public static final long THIRTY_SECONDS = 600L;
        public static final int PASSIVE_EFFECT_THRESHOLD = 100000;
        public static final int POTION_EXPIRY_THRESHOLD = 100;

        private Timing() {}
    }

    public static final class Mana {
        public static final int DEFAULT_MAX = 100;
        public static final int DEFAULT_REGEN = 1;
        public static final String ICON = "";

        private Mana() {}
    }

    public static final class Health {
        public static final double NORMAL_MAX = 20.0;
        public static final double LIFE_MAX = 30.0;

        private Health() {}
    }

    public static final class Animation {
        public static final int ROLL_STEPS = 16;
        public static final long ROLL_DELAY_TICKS = 3L;
        public static final long DOUBLE_TAP_THRESHOLD_MS = 250L;
        public static final long TAP_CHECK_DELAY = 6L;
        public static final long TAP_CLEANUP_DELAY = 2L;
        public static final double MOB_FOLLOW_SPEED = 1.2;

        private Animation() {}
    }

    public static final class Duration {
        public static final long EARTH_TUNNEL_MS = 20_000L;
        public static final long EARTH_CHARM_MS = 30_000L;
        public static final long DEATH_SUMMON_MS = 120_000L;
        public static final long FROST_PUNCH_READY_MS = 10_000L;
        public static final long FROST_FREEZE_MS = 5_000L;
        public static final long FROST_CIRCLE_MS = 10_000L;
        public static final long METAL_CHAIN_STUN_MS = 3_000L;
        public static final long LIFE_ENTANGLE_MS = 3_000L;
        public static final long WATER_PRISON_MS = 3_000L;

        private Duration() {}
    }

    public static final class Distance {
        public static final double AIR_BLAST_RADIUS = 6.0;
        public static final double WATER_GEYSER_RADIUS = 5.0;
        public static final double LIFE_REGEN_RADIUS = 5.0;
        public static final double FROST_CIRCLE_RADIUS = 5.0;
        public static final double DEATH_HUNGER_RADIUS = 5.0;
        public static final double LIFE_CROP_RADIUS = 5.0;
        public static final double FIRE_EXPLOSION_RADIUS = 6.0;
        public static final double MOB_ATTACK_RADIUS = 16.0;
        public static final double MOB_TELEPORT_DISTANCE = 25.0;
        public static final double MOB_FOLLOW_DISTANCE = 3.0;

        private Distance() {}
    }

    public static final class Damage {
        public static final double FIRE_EXPLOSION_DAMAGE = 7.0;

        private Damage() {}
    }

    public static final class Default {
        public static final boolean ADVANCED_REROLLER_ENABLED = true;

        private Default() {}
    }
}


