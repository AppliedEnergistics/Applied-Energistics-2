package appeng.data.providers;


import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;
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
	IBlocks BLOCKS = Api.INSTANCE.definitions().blocks();
	IItems ITEMS = Api.INSTANCE.definitions().items();
	IMaterials MATERIALS = Api.INSTANCE.definitions().materials();

	default Path getPath( Path root, ResourceLocation id )
	{
		return root.resolve( "data/" + id.getNamespace() + "/" + getDataPath() + "/" + id.getPath() + ".json" );
	}

	@Nonnull
	@Override
	default String getName()
	{
		return AppEng.MOD_NAME + "/" + getDataPath();
	}

	@Nonnull
	String getDataPath();

}
