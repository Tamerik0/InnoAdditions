package dev.necr0manthre.innoadditions.datagen;

import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.init.InnoItems;
import dev.necr0manthre.innoadditions.init.InnoMobEffects;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModRuRuLanguageProvider extends LanguageProvider {

    public ModRuRuLanguageProvider(PackOutput output) {
        super(output, Innoadditions.MODID, "ru_ru");
    }

    @Override
    protected void addTranslations() {
        // Эффекты
        add(InnoMobEffects.BOSS.get(), "Босс");

        add(InnoMobEffects.BOSS_DEFENSE_FIRE.get(), "Защита огня");
        add(InnoMobEffects.BOSS_DEFENSE_ICE.get(), "Защита льда");
        add(InnoMobEffects.BOSS_DEFENSE_MAGIC.get(), "Защита магии");
        add(InnoMobEffects.BOSS_DEFENSE_STONE.get(), "Защита камня");
        add(InnoMobEffects.BOSS_DEFENSE_VULN_MELEE.get(), "Защита, слабая к ближнему бою");
        add(InnoMobEffects.BOSS_DEFENSE_VULN_PROJECTILE.get(), "Защита, слабая к снарядам");

        // Предметы
        add(InnoItems.UNCOMMON_SUMMONER.get(), "Призыватель босса (Необычный)");
        add(InnoItems.RARE_SUMMONER.get(), "Призыватель босса (Редкий)");
        add(InnoItems.EPIC_SUMMONER.get(), "Призыватель босса (Эпический)");
        add(InnoItems.MYTHIC_SUMMONER.get(), "Призыватель босса (Мифический)");
    }
}
