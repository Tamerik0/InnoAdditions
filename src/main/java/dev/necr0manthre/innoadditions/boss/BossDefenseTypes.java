package dev.necr0manthre.innoadditions.boss;

import dev.necr0manthre.innoadditions.DamageTypeUtils;
import net.minecraft.world.damagesource.DamageSource;

public interface BossDefenseTypes {
    BossDefenseType FIRE = DamageTypeUtils::isIce;
    BossDefenseType ICE = DamageTypeUtils::isFire;
    BossDefenseType MAGIC = DamageTypeUtils::isMagic;
    BossDefenseType STONE = DamageTypeUtils::isExplosion;
    BossDefenseType VULNERABLE_MELEE = DamageTypeUtils::isMelee;
    BossDefenseType VULNERABLE_PROJECTILE = BossDefenseTypes::isProjectile;

    static boolean isProjectile(DamageSource src) {
        return src != null && src.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE);
    }

}
