package dev.necr0manthre.innoadditions.init;

import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.boss.BossDefenseTypes;
import dev.necr0manthre.innoadditions.mob_effect.BossDefenseMobEffect;
import dev.necr0manthre.innoadditions.mob_effect.BossMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface InnoMobEffects {
    DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Innoadditions.MODID);

    RegistryObject<MobEffect> BOSS = MOB_EFFECTS.register("boss", () -> new BossMobEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));

    RegistryObject<MobEffect> BOSS_DEFENSE_FIRE = MOB_EFFECTS.register("boss_defense_fire", () -> new BossDefenseMobEffect(BossDefenseTypes.FIRE, MobEffectCategory.BENEFICIAL, 0xFF4500, "dad86ff7-e735-4a83-9a9a-cddd18e3d99a"));
    RegistryObject<MobEffect> BOSS_DEFENSE_ICE = MOB_EFFECTS.register("boss_defense_ice", () -> new BossDefenseMobEffect(BossDefenseTypes.ICE, MobEffectCategory.BENEFICIAL, 0x87CEFA, "a0c546c8-c71b-4f5e-8c5c-349b028300ef"));
    RegistryObject<MobEffect> BOSS_DEFENSE_MAGIC = MOB_EFFECTS.register("boss_defense_magic", () -> new BossDefenseMobEffect(BossDefenseTypes.MAGIC, MobEffectCategory.BENEFICIAL, 0x9932CC, "c8f555c8-f99b-4cd3-b219-e1fb70fe5a52"));
    RegistryObject<MobEffect> BOSS_DEFENSE_STONE = MOB_EFFECTS.register("boss_defense_stone", () -> new BossDefenseMobEffect(BossDefenseTypes.STONE, MobEffectCategory.BENEFICIAL, 0x708090, "fbbc8f39-d1ef-4ae9-bfb5-6c913d3c650a"));
    RegistryObject<MobEffect> BOSS_DEFENSE_VULN_MELEE = MOB_EFFECTS.register("boss_defense_vulnerable_melee", () -> new BossDefenseMobEffect(BossDefenseTypes.VULNERABLE_MELEE, MobEffectCategory.BENEFICIAL, 0xB22222, "69ce6589-c50a-432f-b7c0-36895d77ee3b"));
    RegistryObject<MobEffect> BOSS_DEFENSE_VULN_PROJECTILE = MOB_EFFECTS.register("boss_defense_vulnerable_projectile", () -> new BossDefenseMobEffect(BossDefenseTypes.VULNERABLE_PROJECTILE, MobEffectCategory.BENEFICIAL, 0x228B22, "293f98ee-2bb1-4af8-871a-772c7f6529c3"));
}
