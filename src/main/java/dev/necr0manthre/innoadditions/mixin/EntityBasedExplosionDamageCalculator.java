package dev.necr0manthre.innoadditions.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.necr0manthre.innoadditions.boss.BossState;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.world.level.EntityBasedExplosionDamageCalculator.class)
public class EntityBasedExplosionDamageCalculator {
    @WrapOperation(method = "lambda$getBlockExplosionResistance$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;F)F"))
    public float getBlockExplosionResistance(
            Entity instance,
            Explosion explosion,
            BlockGetter blockGetter,
            BlockPos pos,
            BlockState state,
            FluidState fluidState,
            float resistance,
            Operation<Float> original) {
        if (instance instanceof LivingEntity living && living.hasEffect(InnoMobEffects.BOSS.get()))
            return BossState.getOrCreate(living).getBlockExplosionResistance(explosion, blockGetter, pos, state, fluidState, resistance);
        return original.call(instance, explosion, blockGetter, pos, state, fluidState, resistance);
    }
}
