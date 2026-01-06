package dev.necr0manthre.innoadditions;

import com.mojang.logging.LogUtils;
import dev.necr0manthre.innoadditions.init.InnoItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Innoadditions.MODID)
public class Innoadditions {
    public static final String MODID = "innoadditions";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Innoadditions() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        InnoItems.ITEMS.register(modEventBus);

    }
}
