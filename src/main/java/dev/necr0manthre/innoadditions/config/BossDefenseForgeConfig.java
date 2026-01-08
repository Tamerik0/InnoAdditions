package dev.necr0manthre.innoadditions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public final class BossDefenseForgeConfig {

    public static final ForgeConfigSpec SPEC;
    public static final BossDefenseForgeConfig INSTANCE;

    static {
        Pair<BossDefenseForgeConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(BossDefenseForgeConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ForgeConfigSpec.BooleanValue enabled;
    public final ForgeConfigSpec.IntValue levelCount;
    public final ForgeConfigSpec.DoubleValue regenPerSecond;
    public final ForgeConfigSpec.DoubleValue drainTicksPerPercentDamage;
    public final ForgeConfigSpec.DoubleValue specificResistance;

    public final ForgeConfigSpec.ConfigValue<List<? extends Double>> protections;
    public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> durations;

    private BossDefenseForgeConfig(ForgeConfigSpec.Builder b) {
        b.push("boss_defense");

        this.enabled = b.comment("Enable defensive shield effects logic.")
                .define("enabled", true);

        this.levelCount = b.comment("How many amplifier levels are supported for defense effects. 1 => only amplifier 0.")
                .defineInRange("levelCount", 3, 1, 1000);

        b.comment("Shared values for all defense levels (only duration differs per level).");
        this.regenPerSecond = b.comment("How many duration ticks are regenerated per second.")
                .defineInRange("regenTicksPerSecond", 25D, 0D, 20D * 60D * 60D);

        this.drainTicksPerPercentDamage = b.comment("How many duration ticks are drained per 1% of HP counter-damage.")
                .defineInRange("drainTicksPerPercentDamage", 100D, 0D, 20D * 60D * 60D);

        this.specificResistance = b.comment("Resistance to the counter type. 0.7 => only 30% damage goes through.")
                .defineInRange("specificResistance", 0.7D, 0D, 1D);

        this.protections = b
                .defineListAllowEmpty(
                        List.of("protections"),
                        List.of(0.10D, 0.20D, 0.30D),
                        o -> true
                );

        this.durations = b
                .defineListAllowEmpty(
                        List.of("durations"),
                        List.of(100, 100, 60),
                        o -> true
                );

        b.pop();
    }
}
