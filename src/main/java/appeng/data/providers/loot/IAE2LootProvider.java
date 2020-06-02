package appeng.data.providers.loot;


import appeng.data.providers.IAE2DataProvider;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nonnull;


public interface IAE2LootProvider extends IAE2DataProvider
{

	@Nonnull
	@Override
	default LootTable finishBuilding( LootTable.Builder builder )
	{
		return builder.setParameterSet( getParameterSetTarget() ).build();
	}

	LootParameterSet getParameterSetTarget();

}
