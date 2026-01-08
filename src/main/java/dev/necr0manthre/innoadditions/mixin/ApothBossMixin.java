package dev.necr0manthre.innoadditions.mixin;

import dev.necr0manthre.innoadditions.config.BossDefenseForgeConfig;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import dev.necr0manthre.innoadditions.mob_effect.BossDefenseMobEffect;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ApothBoss.class)
public class ApothBossMixin {
    @Unique
    private static LootRarity getUncommon() {
        return RarityRegistry.INSTANCE.getValue(Apotheosis.loc("uncommon"));
    }

    @Unique
    private static LootRarity getRare() {
        return RarityRegistry.INSTANCE.getValue(Apotheosis.loc("rare"));
    }

    @Inject(method = "initBoss", at = @At("TAIL"), remap = false)
    void initBoss(RandomSource rand, Mob entity, float luck, LootRarity rarity, CallbackInfo ci) {
        var effects = new ArrayList<>(InnoMobEffects.MOB_EFFECTS.getEntries().stream().filter(effect -> effect.get() instanceof BossDefenseMobEffect).toList());
        if (rarity.isAtMost(getUncommon()))
            effects.remove(InnoMobEffects.BOSS_DEFENSE_MAGIC);
        if (rarity.isAtMost(getRare()))
            effects.remove(InnoMobEffects.BOSS_DEFENSE_STONE);
        var effect = effects.get(rand.nextInt(effects.size())).get();
        if (effect == InnoMobEffects.BOSS_DEFENSE_ICE.get()) {
            entity.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
        entity.addEffect(new MobEffectInstance(effect, 10, BossDefenseForgeConfig.INSTANCE.levelCount.get() - 1));
    }
}
