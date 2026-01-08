package dev.necr0manthre.innoadditions.mob_effect;

import dev.necr0manthre.innoadditions.boss.BossDefenseType;
import dev.necr0manthre.innoadditions.config.BossDefenseForgeConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BossDefenseMobEffect extends MobEffect {
    private final BossDefenseType defenseType;

    public BossDefenseMobEffect(BossDefenseType defenseType, MobEffectCategory category, int color, String modifierId) {
        super(category, color);
        this.defenseType = defenseType;
        addAttributeModifier(Attributes.ARMOR, modifierId, 1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;
        if (!BossDefenseForgeConfig.INSTANCE.enabled.get()) return;

        MobEffectInstance instance = entity.getEffect(this);
        if (instance == null) return;

        int maxDuration = BossDefenseForgeConfig.INSTANCE.durations.get().get(amplifier);
        int duration = instance.getDuration();

        double regenPerSecond = BossDefenseForgeConfig.INSTANCE.regenPerSecond.get();
        int regen = (int) (entity.tickCount * regenPerSecond / 20) - (int) ((entity.tickCount - 1) * regenPerSecond / 20);
        duration += regen;

        while (duration > maxDuration) {
            if (amplifier == BossDefenseForgeConfig.INSTANCE.levelCount.get() - 1)
                duration = maxDuration;
            else {
                amplifier++;
                duration -= maxDuration = BossDefenseForgeConfig.INSTANCE.durations.get().get(amplifier);
            }
        }
        duration += 1;
        entity.forceAddEffect(new MobEffectInstance(this, duration, amplifier, true, false, true), null);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return BossDefenseForgeConfig.INSTANCE.protections.get().get(amplifier);
    }

    private void handleDamage(MobEffectInstance instance, LivingHurtEvent event) {
        int amplifier = instance.getAmplifier();

        if (!defenseType.checkDamageType(event.getSource())) return;

        float resist = BossDefenseForgeConfig.INSTANCE.specificResistance.get().floatValue();
        event.setAmount(Math.max(0, event.getAmount() * (1 - resist)));
        int drain = Mth.floor(BossDefenseForgeConfig.INSTANCE.drainTicksPerPercentDamage.get() * event.getAmount() * 100 / event.getEntity().getMaxHealth());
        int duration = instance.getDuration() - drain;
        while (duration <= 0) {
            if (amplifier == 0) {
                duration = 2;
                break;
            }
            amplifier--;
            duration += BossDefenseForgeConfig.INSTANCE.durations.get().get(amplifier);
        }
        event.getEntity().forceAddEffect(new MobEffectInstance(this, duration, amplifier, true, false, true), null);
    }

    @SubscribeEvent
    public static void onBossHurtDefenseHandling(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!BossDefenseForgeConfig.INSTANCE.enabled.get()) return;

        float amount = event.getAmount();
        if (amount <= 0F) return;

        for (var effect : event.getEntity().getActiveEffects()) {
            if (effect.getEffect() instanceof BossDefenseMobEffect defenseMobEffect)
                defenseMobEffect.handleDamage(effect, event);
        }
    }
}
