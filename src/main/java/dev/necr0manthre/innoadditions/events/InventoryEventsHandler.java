package dev.necr0manthre.innoadditions.events;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import dev.necr0manthre.innoadditions.config.BossDefenseForgeConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber
public class InventoryEventsHandler {
    private static final Map<Integer, ResourceLocation> raritiesByTier = Map.of(
            1, Apotheosis.loc("common"),
            2, Apotheosis.loc("uncommon"),
            3, Apotheosis.loc("rare"),
            4, Apotheosis.loc("epic"),
            5, Apotheosis.loc("mythic"),
            6, Apotheosis.loc("ancient")
    );

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player))
            return;
        if (BossDefenseForgeConfig.INSTANCE.stepan.get().contains(player.getScoreboardName()))
            return;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            var isMagicUnlocked = player.getCapability(PlayerMagicProvider.MAGIC).map(IPlayerMagic::isMagicUnlocked).orElse(false);
            var tier = isMagicUnlocked ?
                    player.getCapability(PlayerProgressionProvider.PROGRESSION).map(IPlayerProgression::getTier).orElse(0) + 1
                    : 1;
            if (tier == 6)
                return;
            var maxRarity = RarityRegistry.INSTANCE.getOrDefault(raritiesByTier.getOrDefault(tier, null), null);
            if (AffixHelper.getRarity(stack).getOptional().map(rarity -> maxRarity == null || !rarity.isAtMost(maxRarity)).orElse(false)) {
                player.drop(stack.copy(), true);
                player.getInventory().setItem(i, ItemStack.EMPTY);
                player.sendSystemMessage(Component.translatable("innoadditions.lox"));
            }
        }
    }
}
