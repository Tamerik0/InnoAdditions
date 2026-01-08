package dev.necr0manthre.innoadditions.mob_effect;

import dev.necr0manthre.innoadditions.boss.BossState;
import dev.necr0manthre.innoadditions.config.BossEffectForgeConfig;
import dev.necr0manthre.innoadditions.config.BossEffectLevelConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * "Boss" effect.
 *
 * All boss ticking logic is executed from {@link #applyEffectTick}.
 * Non-ticking bookkeeping (hits/retaliation) is handled by boss event subscribers.
 */
public class BossMobEffect extends MobEffect {

    public BossMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        BossEffectLevelConfig cfg = BossEffectForgeConfig.INSTANCE.forAmplifier(amplifier);
        BossState.getOrCreate(entity).tick(cfg);
    }
}
