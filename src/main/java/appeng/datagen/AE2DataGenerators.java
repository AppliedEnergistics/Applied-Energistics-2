package appeng.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import appeng.core.AppEng;
import appeng.datagen.providers.loot.BlockDropProvider;
import appeng.datagen.providers.recipes.SlabStairRecipes;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;

@Mod.EventBusSubscriber(modid = AppEng.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AE2DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent dataEvent) {
        DataGenerator generator = dataEvent.getGenerator();
        if (dataEvent.includeServer()) {
            generator.addProvider(new BlockDropProvider(dataEvent));
            generator.addProvider(new SlabStairRecipes(generator));
            BlockTagsProvider blockTagsProvider = new BlockTagsProvider(dataEvent);
            generator.addProvider(blockTagsProvider);
            generator.addProvider(new ItemTagsProvider(dataEvent, blockTagsProvider));
        }
    }

}
