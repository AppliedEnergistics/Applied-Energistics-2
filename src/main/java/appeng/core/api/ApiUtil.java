
package appeng.core.api;


import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IUtilApi;
import appeng.core.localization.GuiText;
import appeng.util.Platform;


public class ApiUtil implements IUtilApi
{
	@Override
	public <T extends IAEStack<T>> void addCellInformation( ICellInventoryHandler<T> handler, List<String> lines )
	{
		if( handler == null )
		{
			return;
		}

		final ICellInventory<?> cellInventory = handler.getCellInv();

		if( cellInventory != null )
		{
			lines.add( cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );

			lines.add( cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types
					.getLocal() );
		}

		if( handler.isPreformatted() )
		{
			final String list = ( handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded ).getLocal();

			if( handler.isFuzzy() )
			{
				lines.add( GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Fuzzy.getLocal() );
			}
			else
			{
				lines.add( GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal() );
			}
		}

	}

	@Override
	public <T extends IAEStack<T>> T poweredInsert( IEnergySource energy, IMEInventory<T> inv, T input, IActionSource src, Actionable mode )
	{
		return Platform.poweredInsert( energy, inv, input, src, mode );
	}

	@Override
	public <T extends IAEStack<T>> T poweredExtraction( IEnergySource energy, IMEInventory<T> inv, T request, IActionSource src, Actionable mode )
	{
		return Platform.poweredExtraction( energy, inv, request, src, mode );
	}
}
