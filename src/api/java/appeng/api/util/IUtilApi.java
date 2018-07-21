
package appeng.api.util;


import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStack;


public interface IUtilApi
{
	/**
	 * Add cell information to the provided list. Used for tooltip content.
	 * 
	 * @param handler Cell handler.
	 * @param lines List of lines to add to.
	 */
	<T extends IAEStack<T>> void addCellInformation( ICellInventoryHandler<T> handler, List<String> lines );

	/**
	 * Extracts items from a {@link IMEInventory} respecting power requirements.
	 * 
	 * @param energy Energy source.
	 * @param inv Inventory to extract from.
	 * @param request Requested item and count.
	 * @param src Action source.
	 * @param mode Simulate or modulate
	 * @return extracted items or {@code null} of nothing was extracted.
	 */
	<T extends IAEStack<T>> T poweredExtraction( final IEnergySource energy, final IMEInventory<T> inv, final T request, final IActionSource src, final Actionable mode );

	/**
	 * Inserts items into a {@link IMEInventory} respecting power requirements.
	 * 
	 * @param energy Energy source.
	 * @param inv Inventory to insert into.
	 * @param request Items to insert.
	 * @param src Action source.
	 * @param mode Simulate or modulate
	 * @return items not inserted or {@code null} if everything was inserted.
	 */
	<T extends IAEStack<T>> T poweredInsert( final IEnergySource energy, final IMEInventory<T> inv, final T input, final IActionSource src, final Actionable mode );
}
