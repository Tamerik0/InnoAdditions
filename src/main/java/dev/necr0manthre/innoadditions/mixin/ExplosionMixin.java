package dev.necr0manthre.innoadditions.mixin;

import dev.necr0manthre.innoadditions.boss.BossExplosionDamageCalculator;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
//лютый костыль, написанный ИИшкой, но оно работает, а это главное)
@Mixin(Explosion.class)
public class ExplosionMixin {

    @Unique
    private static final Field innoadditions$DAMAGE_CALCULATOR_FIELD;

    static {
        Field f = null;
        try {
            // Find non-static field of type ExplosionDamageCalculator
            for (Field field : Explosion.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getType() == ExplosionDamageCalculator.class) {
                    field.setAccessible(true);
                    f = field;
                    break;
                }
            }
        } catch (Exception e) {
            // Log?
        }
        innoadditions$DAMAGE_CALCULATOR_FIELD = f;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V", at = @At("RETURN"))
    private void onConstruct(Level level, Entity entity, DamageSource source, ExplosionDamageCalculator calculator, double x, double y, double z, float radius, boolean fire, Explosion.BlockInteraction interaction, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && living.hasEffect(InnoMobEffects.BOSS.get())) {
            if (innoadditions$DAMAGE_CALCULATOR_FIELD != null) {
                try {
                    innoadditions$DAMAGE_CALCULATOR_FIELD.set(this, new BossExplosionDamageCalculator(living));
                } catch (Exception e) {
                    // Ignore or log
                }
            }
        }
    }
}

