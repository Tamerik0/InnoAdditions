package dev.necr0manthre.innoadditions.config;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigEvents {
    @SubscribeEvent
    public static void onConfigEvent(ModConfigEvent event) {
        if (!(event instanceof ModConfigEvent.Unloading))
            BossEffectForgeConfig.INSTANCE.rebuildResolved();
    }
}
