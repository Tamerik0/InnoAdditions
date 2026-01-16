package dev.necr0manthre.innoadditions.events;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import dev.necr0manthre.innoadditions.config.BossDefenseForgeConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.common.CuriosHelper;

import java.util.Map;
import java.util.Optional;

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

        var isMagicUnlocked = player.getCapability(PlayerMagicProvider.MAGIC).map(IPlayerMagic::isMagicUnlocked).orElse(false);
        var tier = isMagicUnlocked ?
                player.getCapability(PlayerProgressionProvider.PROGRESSION).map(IPlayerProgression::getTier).orElse(0) + 1
                : 1;
        if (tier == 6)
            return;

        var maxRarity = RarityRegistry.INSTANCE.getOrDefault(raritiesByTier.getOrDefault(tier, null), null);

        // Check Armor and Hands
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (getEffectiveRarity(stack).map(rarity -> maxRarity == null || !rarity.isAtMost(maxRarity)).orElse(false)) {
                player.drop(stack.copy(), true);
                player.setItemSlot(slot, ItemStack.EMPTY);
                player.sendSystemMessage(Component.translatable("innoadditions.lox"));
            }
        }

        // Check Curios
        new CuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getCurios().forEach((id, stacksHandler) -> {
                for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (getEffectiveRarity(stack).map(rarity -> maxRarity == null || !rarity.isAtMost(maxRarity)).orElse(false)) {
                        player.drop(stack.copy(), true);
                        stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                        player.sendSystemMessage(Component.translatable("innoadditions.lox"));
                    }
                }
            });
        });
    }

    private static Optional<LootRarity> getEffectiveRarity(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack).getOptional().orElse(null);
        for (var affix : AffixHelper.streamAffixes(stack).toList()) {
            var affixRarity = affix.rarity().getOptional().orElse(null);
            if (affixRarity != null && (rarity == null || affixRarity.isAtLeast(rarity)))
                rarity = affixRarity;
        }
        for (var gem : SocketHelper.getGems(stack)) {
            var affixRarity = gem.rarity().getOptional().orElse(null);
            if (affixRarity != null && (rarity == null || affixRarity.isAtLeast(rarity)))
                rarity = affixRarity;
        }
        return Optional.ofNullable(rarity);
    }
}
