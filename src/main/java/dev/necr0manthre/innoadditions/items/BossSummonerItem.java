package dev.necr0manthre.innoadditions.items;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BossSummonerItem extends Item {
    private final String rarityName;


    public BossSummonerItem(Properties properties, String rarityName) {
        super(properties);
        this.rarityName = rarityName;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        if (world.isClientSide) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        var rarity = RarityRegistry.INSTANCE.getValue(Apotheosis.loc(rarityName));
        if (rarity == null)
            return InteractionResult.FAIL;
        ApothBoss item = BossRegistry.INSTANCE.getRandomItem(world.getRandom(), ctx.getPlayer().getLuck(), WeightedDynamicRegistry.IDimensional.matches(world), boss -> boss.getMinRarity().isAtMost(rarity) && boss.getMaxRarity().isAtLeast(rarity), IStaged.matches(player));
        if (item == null) return InteractionResult.FAIL;
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (!world.noCollision(item.getSize().move(pos))) {
            pos = pos.above();
            if (!world.noCollision(item.getSize().move(pos))) return InteractionResult.FAIL;
        }
        Mob boss = item.createBoss((ServerLevel) world, pos, world.getRandom(), player.getLuck(), rarity);
        boss.setTarget(player);
        ((ServerLevel) world).addFreshEntityWithPassengers(boss);
        ctx.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

}
