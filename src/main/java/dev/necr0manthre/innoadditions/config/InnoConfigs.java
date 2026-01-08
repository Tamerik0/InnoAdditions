package dev.necr0manthre.innoadditions.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class InnoConfigs {
    private InnoConfigs() {
    }

    public static void register() {
        // Note: ModLoadingContext.get() is deprecated in newer Forge, but still works on this setup.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BossEffectForgeConfig.SPEC, "innoadditions-boss-effect.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BossDefenseForgeConfig.SPEC, "innoadditions-boss-defense.toml");
    }
}