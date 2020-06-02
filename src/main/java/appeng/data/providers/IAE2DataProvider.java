package appeng.data.providers;


import appeng.core.AppEng;
import com.google.gson.JsonElement;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;

import javax.annotation.Nonnull;
import java.nio.file.Path;


public interface IAE2DataProvider extends IDataProvider
{

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

	default JsonElement toJson( LootTable.Builder builder )
	{
		return LootTableManager.toJson( finishBuilding( builder ) );
	}

	@Nonnull
	LootTable finishBuilding( LootTable.Builder builder );

	@Nonnull
	String getDataPath();

}
