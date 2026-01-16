package dev.necr0manthre.innoadditions.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class BossExplosionDamageCalculator extends EntityBasedExplosionDamageCalculator {
    private final LivingEntity boss;
    private static final ExplosionDamageCalculator DEFAULT = new ExplosionDamageCalculator();

    public BossExplosionDamageCalculator(LivingEntity boss) {
        super(boss);
        this.boss = boss;
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
        Optional<Float> base = DEFAULT.getBlockExplosionResistance(explosion, reader, pos, state, fluid);
        return base.map(resistance -> BossState.getOrCreate(boss).getBlockExplosionResistance(explosion, reader, pos, state, fluid, resistance));
    }
}

