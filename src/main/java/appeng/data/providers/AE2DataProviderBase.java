package appeng.data.providers;


import appeng.core.AppEng;
import com.google.gson.JsonElement;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;

import javax.annotation.Nonnull;
import java.nio.file.Path;


public abstract class AE2DataProviderBase implements IDataProvider
{

	protected Path getPath( Path root, ResourceLocation id )
	{
		return root.resolve( "data/" + id.getNamespace() + "/" + getDataPath() + "/" + id.getPath() + ".json" );
	}

	@Nonnull
	@Override
	public String getName()
	{
		return AppEng.MOD_NAME + "/" + getDataPath();
	}

	protected JsonElement toJson( LootTable.Builder builder )
	{
		return LootTableManager.toJson( finishBuilding( builder ) );
	}

	@Nonnull
	protected abstract LootTable finishBuilding( LootTable.Builder builder );

	@Nonnull
	abstract protected String getDataPath();

}
