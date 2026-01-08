package dev.necr0manthre.innoadditions;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public interface DamageTypeUtils {
    static boolean isFire(DamageSource src) {
        return src != null && src.is(DamageTypeTags.IS_FIRE);
    }

    static boolean isExplosion(DamageSource src) {
        return src != null && src.is(DamageTypeTags.IS_EXPLOSION);
    }

    static boolean isMelee(DamageSource src) {
        if (src == null) return false;
        return src.is(DamageTypes.PLAYER_ATTACK) || src.is(DamageTypes.MOB_ATTACK);
    }

    static boolean isMagic(DamageSource src) {
        if (src == null) return false;
        return src.is(DamageTypes.MAGIC) || src.is(DamageTypes.INDIRECT_MAGIC);
    }

    static boolean isIce(DamageSource src) {
        return src != null && src.is(DamageTypeTags.IS_FREEZING);
    }
}
