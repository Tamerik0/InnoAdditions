package dev.necr0manthre.innoadditions.boss;

import dev.necr0manthre.innoadditions.DamageTypeUtils;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Records when the boss actually damages a player (retaliation).
 * Aura damage is excluded: it's player being damaged by the boss, but not via a direct entity attack event.
 */
@Mod.EventBusSubscriber(modid = dev.necr0manthre.innoadditions.Innoadditions.MODID)
public final class BossRetaliationEventHandlers {

    private static boolean hasBossEffect(Mob mob) {
        MobEffectInstance inst = mob.getEffect(InnoMobEffects.BOSS.get());
        return inst != null;
    }

    @SubscribeEvent
    public static void onBossDamagesPlayer(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DamageSource src = event.getSource();
        if (!(src.getEntity() instanceof Mob boss)) return;
        if (!hasBossEffect(boss)) return;

        if (DamageTypeUtils.isExplosion(src)) return;

        BossState.getOrCreate(boss).recordBossHitPlayer(player);
    }
}

