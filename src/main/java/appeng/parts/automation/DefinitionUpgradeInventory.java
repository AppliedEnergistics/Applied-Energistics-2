package appeng.parts.automation;


import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.tile.inventory.IAEAppEngInventory;
import net.minecraft.item.ItemStack;


public final class DefinitionUpgradeInventory extends UpgradeInventory
{
	private final IItemDefinition definition;

	public DefinitionUpgradeInventory( final IItemDefinition definition, final IAEAppEngInventory parent, final int s )
	{
		super( parent, s );

		this.definition = definition;
	}

	@Override
	public int getMaxInstalled( final Upgrades upgrades )
	{
		int max = 0;

		for( final ItemStack stack : upgrades.getSupported().keySet() )
		{
			if( this.definition.isSameAs( stack ) )
			{
				max = upgrades.getSupported().get( stack );
				break;
			}
		}

		return max;
	}
}
