package dev.necr0manthre.innoadditions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge config for the boss mob effect.
 *
 * Supports multiple "levels" = MobEffect amplifier values.
 */
public final class BossEffectForgeConfig {

    public static final ForgeConfigSpec SPEC;
    public static final BossEffectForgeConfig INSTANCE;

    static {
        Pair<BossEffectForgeConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(BossEffectForgeConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    /** How many amplifier levels are supported (1 => amplifier 0 only). */
    public final ForgeConfigSpec.IntValue levelCount;

    /** Default template values (used for new levels and fallbacks). */
    private final LevelSpec defaults;

    /** Max supported levels we expose as sections. */
    private static final int MAX_LEVELS = 64;

    /** Per-level config specs (level0..level63). */
    private final List<LevelSpec> levelSpecs = new ArrayList<>(MAX_LEVELS);

    /** Resolved runtime configs. Size == levelCount. */
    private final List<BossEffectLevelConfig> resolvedLevels = new ArrayList<>();

    private BossEffectForgeConfig(ForgeConfigSpec.Builder b) {
        b.push("boss_effect");

        this.levelCount = b.comment("How many effect levels (amplifiers) are supported. 1 => only amplifier 0.")
                .defineInRange("levelCount", 1, 1, MAX_LEVELS);

        // Defaults used for level0 initial values.
        defaults = new LevelSpec(b, "defaults", true);

        b.push("levels");
        for (int i = 0; i < MAX_LEVELS; i++) {
            levelSpecs.add(new LevelSpec(b, "level" + i, false));
        }
        b.pop();

        b.pop();
    }

    /** Call this after config load/reload. */
    public void rebuildResolved() {
        resolvedLevels.clear();
        int count = Math.max(1, Math.min(levelCount.get(), MAX_LEVELS));
        for (int amp = 0; amp < count; amp++) {
            resolvedLevels.add(levelSpecs.get(amp).toRuntime(defaults));
        }
    }

    public BossEffectLevelConfig forAmplifier(int amplifier) {
        // If we're queried before config load event, fall back to defaults only.
        if (resolvedLevels.isEmpty()) {
            return levelSpecs.get(0).toRuntime(defaults);
        }
        int idx = Math.max(0, Math.min(amplifier, resolvedLevels.size() - 1));
        return resolvedLevels.get(idx);
    }

    private static final class LevelSpec {
        // Core
        private final ForgeConfigSpec.DoubleValue noPlayersRegenPerTick;
        private final ForgeConfigSpec.DoubleValue baseRegenPerSecond;
        private final ForgeConfigSpec.DoubleValue regenPerAttackerPerSecond;
        private final ForgeConfigSpec.IntValue maxAttackersForScaling;
        private final ForgeConfigSpec.DoubleValue nearbyPlayerRadius;

        // Aura
        private final ForgeConfigSpec.DoubleValue auraRadius;
        private final ForgeConfigSpec.IntValue auraIntervalTicks;
        private final ForgeConfigSpec.DoubleValue auraDamageFraction;

        // Teleport (legacy + positioning)
        private final ForgeConfigSpec.IntValue hitsRequiredForTeleport;
        private final ForgeConfigSpec.IntValue maxTeleportsTracked;
        private final ForgeConfigSpec.DoubleValue teleportMinDist;
        private final ForgeConfigSpec.DoubleValue teleportMaxDist;

        // Unreachable multipliers
        private final ForgeConfigSpec.DoubleValue unreachableRegenMultiplier;
        private final ForgeConfigSpec.DoubleValue unreachableAuraMultiplier;

        // Attacker window
        private final ForgeConfigSpec.IntValue attackerWindowTicks;

        // Teleport trigger
        private final ForgeConfigSpec.IntValue teleportCheckIntervalTicks;
        private final ForgeConfigSpec.DoubleValue teleportBaseChance;
        private final ForgeConfigSpec.DoubleValue teleportChancePerHitWithoutRetaliation;
        private final ForgeConfigSpec.DoubleValue teleportChanceBonusIfPlayerUnreachable;

        // Explosion trigger
        private final ForgeConfigSpec.IntValue explosionCheckIntervalTicks;
        private final ForgeConfigSpec.DoubleValue explosionBaseChance;
        private final ForgeConfigSpec.DoubleValue explosionChancePerHitWithoutRetaliation;
        private final ForgeConfigSpec.DoubleValue explosionChanceBonusIfPlayerUnreachable;
        private final ForgeConfigSpec.DoubleValue explosionStrength;

        LevelSpec(ForgeConfigSpec.Builder b, String name, boolean isDefaults) {
            b.push(name);

            // IMPORTANT: Do NOT call .get() on other ConfigValues here.
            // During spec build, values aren't available yet.

            // Defaults section uses hardcoded defaults.
            // Per-level sections use neutral defaults; real fallback is applied in toRuntime(defaults).
            final double dNoPlayersRegenPerTick = isDefaults ? 0.01D : 0D;
            final double dBaseRegenPerSecond = isDefaults ? 0.005D : 0D;
            final double dRegenPerAttackerPerSecond = isDefaults ? 0.0025D : 0D;
            final int dMaxAttackers = isDefaults ? 10 : 0;
            final double dNearbyPlayerRadius = isDefaults ? 32D : 0D;

            final double dAuraRadius = isDefaults ? 8D : 0D;
            final int dAuraInterval = 40;
            final double dAuraDamageFraction = isDefaults ? 0.01D : 0D;

            final int dHitsRequiredForTeleport = 5;
            final int dMaxTeleportsTracked = 3;
            final double dTeleportMinDist = 2D;
            final double dTeleportMaxDist = 6D;

            final double dUnreachableRegenMult = 2D;
            final double dUnreachableAuraMult = isDefaults ? 2D : 2D;

            final int dAttackerWindowTicks = isDefaults ? 20 * 60 : 20 * 60;

            final int dTeleportCheckIntervalTicks = isDefaults ? 40 : 40;
            final double dTeleportBaseChance = isDefaults ? 0.1D : 0D;
            final double dTeleportChancePerHit = isDefaults ? 0.02D : 0D;
            final double dTeleportChanceBonusUnreachable = isDefaults ? 0.25D : 0D;

            final int dExplosionCheckIntervalTicks = isDefaults ? 60 : 60;
            final double dExplosionBaseChance = isDefaults ? 0.08D : 0D;
            final double dExplosionChancePerHit = isDefaults ? 0.015D : 0D;
            final double dExplosionChanceBonusUnreachable = isDefaults ? 0.2D : 0D;
            final double dExplosionRadius = isDefaults ? 3.5D : 0D;
            final double dExplosionStrength = isDefaults ? 4D : 0D;

            this.noPlayersRegenPerTick = b.comment("Regen per tick (fraction of max HP) when there are no nearby players.")
                    .defineInRange("noPlayersRegenPerTick", dNoPlayersRegenPerTick, 0D, 100D);

            this.baseRegenPerSecond = b.comment("Base regen per second (fraction of max HP) when players are nearby.")
                    .defineInRange("baseRegenPerSecond", dBaseRegenPerSecond, 0D, 100D);

            this.regenPerAttackerPerSecond = b.comment("Extra regen per second per unique attacker in the last window.")
                    .defineInRange("regenPerAttackerPerSecond", dRegenPerAttackerPerSecond, 0D, 100D);

            this.maxAttackersForScaling = b.comment("Max unique attackers counted for regen scaling.")
                    .defineInRange("maxAttackersForScaling", dMaxAttackers, 0, 1000);

            this.nearbyPlayerRadius = b.comment("Radius to consider \"nearby players\"; if none, use noPlayersRegenPerTick.")
                    .defineInRange("nearbyPlayerRadius", dNearbyPlayerRadius, 0D, 1024D);

            this.auraRadius = b.comment("Aura radius (blocks).")
                    .defineInRange("auraRadius", dAuraRadius, 0D, 1024D);

            this.auraIntervalTicks = b.comment("Aura interval in ticks.")
                    .defineInRange("auraIntervalTicks", dAuraInterval, 1, 20 * 60 * 60);

            this.auraDamageFraction = b.comment("Aura damage as fraction of boss max HP per pulse.")
                    .defineInRange("auraDamageFraction", dAuraDamageFraction, 0D, 100D);

            this.hitsRequiredForTeleport = b.comment("(Legacy) Hits required for teleport condition (still used as a scaling reference).")
                    .defineInRange("hitsRequiredForTeleport", dHitsRequiredForTeleport, 1, 1000);

            this.maxTeleportsTracked = b.comment("Max teleports tracked for unreachable buff.")
                    .defineInRange("maxTeleportsTracked", dMaxTeleportsTracked, 1, 1000);

            this.teleportMinDist = b.comment("Teleport min distance.")
                    .defineInRange("teleportMinDist", dTeleportMinDist, 0D, 128D);

            this.teleportMaxDist = b.comment("Teleport max distance.")
                    .defineInRange("teleportMaxDist", dTeleportMaxDist, 0D, 128D);

            this.unreachableRegenMultiplier = b.comment("Regen multiplier when unreachable buff activates.")
                    .defineInRange("unreachableRegenMultiplier", dUnreachableRegenMult, 0D, 100D);

            this.unreachableAuraMultiplier = b.comment("Aura damage multiplier when unreachable buff activates.")
                    .defineInRange("unreachableAuraMultiplier", dUnreachableAuraMult, 0D, 100D);

            this.attackerWindowTicks = b.comment("Attacker window length in ticks.")
                    .defineInRange("attackerWindowTicks", dAttackerWindowTicks, 1, 20 * 60 * 60);

            this.teleportCheckIntervalTicks = b.comment("How often (ticks) boss rolls for teleport.")
                    .defineInRange("teleportCheckIntervalTicks", dTeleportCheckIntervalTicks, 1, 20 * 60 * 60);

            this.teleportBaseChance = b.comment("Base teleport chance per roll (0..1).")
                    .defineInRange("teleportBaseChance", dTeleportBaseChance, 0D, 1D);

            this.teleportChancePerHitWithoutRetaliation = b.comment("Additional teleport chance per 'hit without retaliation' stack (0..1).")
                    .defineInRange("teleportChancePerHitWithoutRetaliation", dTeleportChancePerHit, 0D, 1D);

            this.teleportChanceBonusIfPlayerUnreachable = b.comment("Bonus teleport chance if boss can't path to target player (0..1).")
                    .defineInRange("teleportChanceBonusIfPlayerUnreachable", dTeleportChanceBonusUnreachable, 0D, 1D);

            this.explosionCheckIntervalTicks = b.comment("How often (ticks) boss rolls for block-breaking explosion.")
                    .defineInRange("explosionCheckIntervalTicks", dExplosionCheckIntervalTicks, 1, 20 * 60 * 60);

            this.explosionBaseChance = b.comment("Base explosion chance per roll (0..1).")
                    .defineInRange("explosionBaseChance", dExplosionBaseChance, 0D, 1D);

            this.explosionChancePerHitWithoutRetaliation = b.comment("Additional explosion chance per 'hit without retaliation' stack (0..1).")
                    .defineInRange("explosionChancePerHitWithoutRetaliation", dExplosionChancePerHit, 0D, 1D);

            this.explosionChanceBonusIfPlayerUnreachable = b.comment("Bonus explosion chance if boss can't path to target player (0..1).")
                    .defineInRange("explosionChanceBonusIfPlayerUnreachable", dExplosionChanceBonusUnreachable, 0D, 1D);

            this.explosionStrength = b.comment("Explosion strength (power). Typically 2..6.")
                    .defineInRange("explosionStrength", dExplosionStrength, 0D, 64D);

            b.pop();
        }

        BossEffectLevelConfig toRuntime(LevelSpec fallback) {
            // If this is a per-level section, treat 0 values as "unset" and fall back to defaults.
            // For ints, we use 0 as "unset" only where 0 isn't a valid meaningful value.

            float noPlayersRegenPerTickV = valOrFallback(noPlayersRegenPerTick.get().floatValue(), fallback.noPlayersRegenPerTick.get().floatValue(), 0F);
            float baseRegenPerSecondV = valOrFallback(baseRegenPerSecond.get().floatValue(), fallback.baseRegenPerSecond.get().floatValue(), 0F);
            float regenPerAttackerPerSecondV = valOrFallback(regenPerAttackerPerSecond.get().floatValue(), fallback.regenPerAttackerPerSecond.get().floatValue(), 0F);

            int maxAttackersForScalingV = intOrFallback(maxAttackersForScaling.get(), fallback.maxAttackersForScaling.get(), 0);
            double nearbyPlayerRadiusV = doubleOrFallback(nearbyPlayerRadius.get(), fallback.nearbyPlayerRadius.get(), 0D);

            double auraRadiusV = doubleOrFallback(auraRadius.get(), fallback.auraRadius.get(), 0D);
            int auraIntervalTicksV = intOrFallback(auraIntervalTicks.get(), fallback.auraIntervalTicks.get(), 0);
            float auraDamageFractionV = valOrFallback(auraDamageFraction.get().floatValue(), fallback.auraDamageFraction.get().floatValue(), 0F);

            int hitsRequiredForTeleportV = intOrFallback(hitsRequiredForTeleport.get(), fallback.hitsRequiredForTeleport.get(), 0);
            int maxTeleportsTrackedV = intOrFallback(maxTeleportsTracked.get(), fallback.maxTeleportsTracked.get(), 0);
            double teleportMinDistV = doubleOrFallback(teleportMinDist.get(), fallback.teleportMinDist.get(), 0D);
            double teleportMaxDistV = doubleOrFallback(teleportMaxDist.get(), fallback.teleportMaxDist.get(), 0D);

            float unreachableRegenMultiplierV = valOrFallback(unreachableRegenMultiplier.get().floatValue(), fallback.unreachableRegenMultiplier.get().floatValue(), 0F);
            float unreachableAuraMultiplierV = valOrFallback(unreachableAuraMultiplier.get().floatValue(), fallback.unreachableAuraMultiplier.get().floatValue(), 0F);

            int attackerWindowTicksV = intOrFallback(attackerWindowTicks.get(), fallback.attackerWindowTicks.get(), 0);

            int teleportCheckIntervalTicksV = intOrFallback(teleportCheckIntervalTicks.get(), fallback.teleportCheckIntervalTicks.get(), 0);
            double teleportBaseChanceV = doubleOrFallback(teleportBaseChance.get(), fallback.teleportBaseChance.get(), 0D);
            double teleportChancePerHitWithoutRetaliationV = doubleOrFallback(teleportChancePerHitWithoutRetaliation.get(), fallback.teleportChancePerHitWithoutRetaliation.get(), 0D);
            double teleportChanceBonusIfPlayerUnreachableV = doubleOrFallback(teleportChanceBonusIfPlayerUnreachable.get(), fallback.teleportChanceBonusIfPlayerUnreachable.get(), 0D);

            int explosionCheckIntervalTicksV = intOrFallback(explosionCheckIntervalTicks.get(), fallback.explosionCheckIntervalTicks.get(), 0);
            double explosionBaseChanceV = doubleOrFallback(explosionBaseChance.get(), fallback.explosionBaseChance.get(), 0D);
            double explosionChancePerHitWithoutRetaliationV = doubleOrFallback(explosionChancePerHitWithoutRetaliation.get(), fallback.explosionChancePerHitWithoutRetaliation.get(), 0D);
            double explosionChanceBonusIfPlayerUnreachableV = doubleOrFallback(explosionChanceBonusIfPlayerUnreachable.get(), fallback.explosionChanceBonusIfPlayerUnreachable.get(), 0D);
            float explosionStrengthV = valOrFallback(explosionStrength.get().floatValue(), fallback.explosionStrength.get().floatValue(), 0F);

            return new BossEffectLevelConfig(
                    noPlayersRegenPerTickV,
                    baseRegenPerSecondV,
                    regenPerAttackerPerSecondV,
                    maxAttackersForScalingV,
                    nearbyPlayerRadiusV,

                    auraRadiusV,
                    auraIntervalTicksV,
                    auraDamageFractionV,

                    hitsRequiredForTeleportV,
                    maxTeleportsTrackedV,
                    teleportMinDistV,
                    teleportMaxDistV,

                    unreachableRegenMultiplierV,
                    unreachableAuraMultiplierV,

                    attackerWindowTicksV,

                    teleportCheckIntervalTicksV,
                    teleportBaseChanceV,
                    teleportChancePerHitWithoutRetaliationV,
                    teleportChanceBonusIfPlayerUnreachableV,

                    explosionCheckIntervalTicksV,
                    explosionBaseChanceV,
                    explosionChancePerHitWithoutRetaliationV,
                    explosionChanceBonusIfPlayerUnreachableV,
                    explosionStrengthV
            );
        }

        private static float valOrFallback(float v, float fb, float unset) {
            return v == unset ? fb : v;
        }

        private static double doubleOrFallback(double v, double fb, double unset) {
            return v == unset ? fb : v;
        }

        private static int intOrFallback(int v, int fb, int unset) {
            return v == unset ? fb : v;
        }
    }
}
