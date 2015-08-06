
package appeng.parts.automation;


import net.minecraft.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.tile.inventory.IAEAppEngInventory;


public final class DefinitionUpgradeInventory extends UpgradeInventory
{
	private final IItemDefinition definition;

	public DefinitionUpgradeInventory( IItemDefinition definition, IAEAppEngInventory parent, int s )
	{
		super( parent, s );

		this.definition = definition;
	}

	@Override
	public int getMaxInstalled( Upgrades upgrades )
	{
		int max = 0;

		for( ItemStack stack : upgrades.getSupported().keySet() )
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
