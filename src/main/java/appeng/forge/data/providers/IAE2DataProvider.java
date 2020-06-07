package appeng.forge.data.providers;


import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.AppEng;
import com.google.gson.JsonElement;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;

import javax.annotation.Nonnull;
import java.nio.file.Path;


public interface IAE2DataProvider extends IDataProvider
{
	IBlocks BLOCKS = AEApi.instance().definitions().blocks();
	IItems ITEMS = AEApi.instance().definitions().items();
	IMaterials MATERIALS = AEApi.instance().definitions().materials();
}
