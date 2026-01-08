package dev.necr0manthre.innoadditions.config;

/**
 * Fully resolved (runtime) configuration for one level (amplifier) of the boss effect.
 */
public record BossEffectLevelConfig(
        float noPlayersRegenPerTick,
        float baseRegenPerSecond,
        float regenPerAttackerPerSecond,
        int maxAttackersForScaling,
        double nearbyPlayerRadius,

        double auraRadius,
        int auraIntervalTicks,
        float auraDamageFraction,

        int hitsRequiredForTeleport,
        int maxTeleportsTracked,
        double teleportMinDist,
        double teleportMaxDist,

        float unreachableRegenMultiplier,
        float unreachableAuraMultiplier,

        int attackerWindowTicks,

        // --- Teleport trigger ---
        int teleportCheckIntervalTicks,
        double teleportBaseChance,
        double teleportChancePerHitWithoutRetaliation,
        double teleportChanceBonusIfPlayerUnreachable,

        // --- Explosion trigger ---
        int explosionCheckIntervalTicks,
        double explosionBaseChance,
        double explosionChancePerHitWithoutRetaliation,
        double explosionChanceBonusIfPlayerUnreachable,
        float explosionStrength
) {}
