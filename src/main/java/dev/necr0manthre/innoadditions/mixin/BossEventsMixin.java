package dev.necr0manthre.innoadditions.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossEvents.class)
public class BossEventsMixin {
    @Unique
    private static LootRarity getMaxRarity() {
        return RarityRegistry.INSTANCE.getValue(Apotheosis.loc("uncommon"));
    }

    @WrapOperation(
            method = "naturalBosses",
            at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/adventure/boss/ApothBoss;createBoss(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;F)Lnet/minecraft/world/entity/Mob;"),
            remap = false
    )
    private Mob inno$replaceCreateBoss(ApothBoss item,
                                       ServerLevelAccessor sLevel,
                                       BlockPos pos,
                                       RandomSource rand,
                                       float luck,
                                       Operation<Mob> original,
                                       @Local(argsOnly = true) MobSpawnEvent.FinalizeSpawn e,
                                       @Local Player player) {
        var rarity = LootRarity.random(rand, luck, item);
        if (rarity.isAtLeast(getMaxRarity()))
            rarity = getMaxRarity();
        return item.createBoss(sLevel, BlockPos.containing(e.getX() - (double) 0.5F, e.getY(), e.getZ() - (double) 0.5F), rand, player.getLuck(), rarity);
    }

    @Inject(method = "naturalBosses", at = @At(value = "INVOKE", target = "Ldev/shadowsoffire/apotheosis/adventure/boss/ApothBoss;createBoss(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;F)Lnet/minecraft/world/entity/Mob;"), cancellable = true)
    public void huy(MobSpawnEvent.FinalizeSpawn e, CallbackInfo ci, @Local ApothBoss item) {
        if (!item.getMinRarity().isAtMost(getMaxRarity()))
            ci.cancel();
    }
}
