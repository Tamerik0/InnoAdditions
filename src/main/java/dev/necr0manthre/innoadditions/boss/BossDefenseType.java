package dev.necr0manthre.innoadditions.boss;

import net.minecraft.world.damagesource.DamageSource;

public interface BossDefenseType {
    boolean checkDamageType(DamageSource src);
}

