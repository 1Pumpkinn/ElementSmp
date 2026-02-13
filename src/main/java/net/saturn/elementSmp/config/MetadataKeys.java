package net.saturn.elementSmp.config;

public final class MetadataKeys {
    private MetadataKeys() {}

    public static final class Earth {
        public static final String MINE_UNTIL = "earth_mine_until";
        public static final String TUNNELING = "earth_tunneling";

        private Earth() {}
    }

    public static final class Frost {
        public static final String FROZEN = "frost_frozen";
        public static final String CIRCLE_FROZEN = "frost_freezing_circle";

        private Frost() {}
    }

    public static final class Metal {
        public static final String CHAIN_STUN = "metal_chain_stunned";
        public static final String MAGNETIC_ACCUM_UNTIL = "metal_magnetic_accum_until";
        public static final String MAGNETIC_ACCUM_OWNER = "metal_magnetic_accum_owner";
        public static final String MAGNETIC_ACCUM_DAMAGE = "metal_magnetic_accum_damage";

        private Metal() {}
    }

    public static final class Death {
        public static final String SUMMONED_OWNER = "death_summoned_owner";
        public static final String SUMMONED_UNTIL = "death_summoned_until";

        private Death() {}
    }

    public static final class Air {
        private Air() {}
    }

    public static final class Water {
        public static final String PRISON_STUNNED = "water_prison_stunned";
        public static final String LEECHING_TRIDENT = "water_leeching_trident";
        private Water() {}
    }

    public static final class Fire {
        public static final String SCORCH_ACTIVE = "fire_scorch_active";
        private Fire() {}
    }

    public static final class Life {
        public static final String ENTANGLED = "life_entangled";
        private Life() {}
    }
}
