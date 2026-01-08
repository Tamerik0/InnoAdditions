package dev.necr0manthre.innoadditions;

import com.mojang.logging.LogUtils;
import dev.necr0manthre.innoadditions.config.InnoConfigs;
import dev.necr0manthre.innoadditions.init.InnoItems;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Innoadditions.MODID)
public class Innoadditions {
    public static final String MODID = "innoadditions";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public Innoadditions() {
        InnoConfigs.register();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        InnoItems.ITEMS.register(modEventBus);
        InnoMobEffects.MOB_EFFECTS.register(modEventBus);

    }
}
