package dev.necr0manthre.innoadditions.mixin;

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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getBlockExplosionResistance", at = @At("RETURN"), cancellable = true)
    public void getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos pos, BlockState state, FluidState fluidState, float power, CallbackInfoReturnable<Float> cir) {
        var entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living && living.hasEffect(InnoMobEffects.BOSS.get()))
            cir.setReturnValue(BossState.getOrCreate(living).getBlockExplosionResistance(explosion, blockGetter, pos, state, fluidState, power));
    }
}
