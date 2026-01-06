package dev.necr0manthre.innoadditions.datagen;

import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.init.InnoItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Innoadditions.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(InnoItems.UNCOMMON_SUMMONER.get());
        basicItem(InnoItems.RARE_SUMMONER.get());
        basicItem(InnoItems.EPIC_SUMMONER.get());
        basicItem(InnoItems.MYTHIC_SUMMONER.get());
    }
}