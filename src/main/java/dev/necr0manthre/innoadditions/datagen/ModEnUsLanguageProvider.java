package dev.necr0manthre.innoadditions.datagen;

import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.init.InnoItems;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModEnUsLanguageProvider extends LanguageProvider {

    public ModEnUsLanguageProvider(PackOutput output) {
        super(output, Innoadditions.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Effects
        add(InnoMobEffects.BOSS.get(), "Boss");

        add(InnoMobEffects.BOSS_DEFENSE_FIRE.get(), "Fire Defense");
        add(InnoMobEffects.BOSS_DEFENSE_ICE.get(), "Ice Defense");
        add(InnoMobEffects.BOSS_DEFENSE_MAGIC.get(), "Magic Defense");
        add(InnoMobEffects.BOSS_DEFENSE_STONE.get(), "Stone Defense");
        add(InnoMobEffects.BOSS_DEFENSE_VULN_MELEE.get(), "Defense (Weak to Melee)");
        add(InnoMobEffects.BOSS_DEFENSE_VULN_PROJECTILE.get(), "Defense (Weak to Projectiles)");

        // Items
        add(InnoItems.UNCOMMON_SUMMONER.get(), "Uncommon Boss Summoner");
        add(InnoItems.RARE_SUMMONER.get(), "Rare Boss Summoner");
        add(InnoItems.EPIC_SUMMONER.get(), "Epic Boss Summoner");
        add(InnoItems.MYTHIC_SUMMONER.get(), "Mythic Boss Summoner");

        add("innoadditions.lox", "You are too weak to hold this item");
    }
}
