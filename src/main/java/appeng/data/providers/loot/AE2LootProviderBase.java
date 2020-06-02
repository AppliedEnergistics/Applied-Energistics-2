package appeng.data.providers.loot;


import appeng.data.providers.AE2DataProviderBase;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootTable;

import javax.annotation.Nonnull;


public abstract class AE2LootProviderBase extends AE2DataProviderBase
{

	@Nonnull
	@Override
	protected LootTable finishBuilding( LootTable.Builder builder )
	{
		return builder.setParameterSet( getParameterSetTarget() ).build();
	}

	protected abstract LootParameterSet getParameterSetTarget();

}
