package dev.necr0manthre.innoadditions.init;

import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.items.BossSummonerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface InnoItems {
    DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Innoadditions.MODID);
    RegistryObject<BossSummonerItem> UNCOMMON_SUMMONER = ITEMS.register("uncommon_boss_summoner", () -> new BossSummonerItem(new Item.Properties(), "uncommon"));
    RegistryObject<BossSummonerItem> RARE_SUMMONER = ITEMS.register("rare_boss_summoner", () -> new BossSummonerItem(new Item.Properties(), "rare"));
    RegistryObject<BossSummonerItem> EPIC_SUMMONER = ITEMS.register("epic_boss_summoner", () -> new BossSummonerItem(new Item.Properties(), "epic"));
    RegistryObject<BossSummonerItem> MYTHIC_SUMMONER = ITEMS.register("mythic_boss_summoner", () -> new BossSummonerItem(new Item.Properties(), "mythic"));
}
