package dev.necr0manthre.innoadditions.boss;

import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event-only part of the boss logic.
 * <p>
 * All periodic/ticking behavior is inside {@link dev.necr0manthre.innoadditions.mob_effect.BossMobEffect#applyEffectTick}.
 * Here we only collect combat signals (who hit whom) that feed into the tick logic.
 */
@Mod.EventBusSubscriber(modid = dev.necr0manthre.innoadditions.Innoadditions.MODID)
public final class BossEventHandlers {

    private static boolean hasBossEffect(Mob mob) {
        MobEffectInstance inst = mob.getEffect(InnoMobEffects.BOSS.get());
        return inst != null;
    }

    @SubscribeEvent
    public static void onBossHurtByPlayer(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Mob boss)) return;
        if (!hasBossEffect(boss)) return;

        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            BossState.getOrCreate(boss).recordPlayerHitBoss(player);
        }
    }

    /**
     * Used to remember "last target"; actual teleport roll is periodic in tick().
     */
    @SubscribeEvent
    public static void onPlayerAttackedByBoss(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DamageSource src = event.getSource();
        if (!(src.getEntity() instanceof Mob boss)) return;
        if (!hasBossEffect(boss)) return;
        if (src.is(ResourceKey.create(Registries.DAMAGE_TYPE, Innoadditions.rl("boss_aura")))) return;

        BossState.getOrCreate(boss).noteBossAttemptedAttack(player);
    }

    /**
     * Prevent bosses from taking damage from their own block-breaking explosions.
     */
    @SubscribeEvent
    public static void onBossHurtCancelOwnExplosion(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Mob boss)) return;
        if (!hasBossEffect(boss)) return;

        if (BossState.getOrCreate(boss).shouldCancelSelfExplosion(event.getSource())) {
            event.setCanceled(true);
        }
    }
}

