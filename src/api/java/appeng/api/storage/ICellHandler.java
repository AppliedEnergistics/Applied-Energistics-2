/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.implementations.tiles.IChestOrDrive;


/**
 * Registration record for {@link ICellRegistry}
 */
public interface ICellHandler
{

	/**
	 * return true if the provided item is handled by your cell handler. ( AE May choose to skip this method, and just
	 * request a handler )
	 *
	 * @param is to be checked item
	 *
	 * @return return true, if getCellHandler will not return null.
	 */
	boolean isCell( ItemStack is );

	/**
	 * If you cannot handle the provided item, return null
	 *
	 * @param is      a storage cell item.
	 * @param host    anytime the contents of your storage cell changes it should use this to request a save, please
	 *                note, this value can be null.
	 * @param channel the storage channel requested.
	 *
	 * @return a new IMEHandler for the provided item
	 */
	IMEInventoryHandler getCellInventory( ItemStack is, ISaveProvider host, StorageChannel channel );

	/**
	 * @return the ME Chest texture for light pixels this storage cell type, should be 10x10 with 3px of transparent
	 * padding on a 16x16 texture, null is valid if your cell cannot be used in the ME Chest. refer to the
	 * assets for examples.
	 */
	@SideOnly( Side.CLIENT )
	IIcon getTopTexture_Light();

	/**
	 * @return the ME Chest texture for medium pixels this storage cell type, should be 10x10 with 3px of transparent
	 * padding on a 16x16 texture, null is valid if your cell cannot be used in the ME Chest. refer to the
	 * assets for examples.
	 */
	@SideOnly( Side.CLIENT )
	IIcon getTopTexture_Medium();

	/**
	 * @return the ME Chest texture for dark pixels this storage cell type, should be 10x10 with 3px of transparent
	 * padding on a 16x16 texture, null is valid if your cell cannot be used in the ME Chest. refer to the
	 * assets for examples.
	 */
	@SideOnly( Side.CLIENT )
	IIcon getTopTexture_Dark();

	/**
	 * Called when the storage cell is planed in an ME Chest and the user tries to open the terminal side, if your item
	 * is not available via ME Chests simply tell the user they can't use it, or something, other wise you should open
	 * your gui and display the cell to the user.
	 *
	 * @param player      player opening chest gui
	 * @param chest       to be opened chest
	 * @param cellHandler cell handler
	 * @param inv         inventory handler
	 * @param is          item
	 * @param chan        storage channel
	 */
	void openChestGui( EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan );

	/**
	 * 0 - cell is missing.
	 *
	 * 1 - green, ( usually means available room for types or items. )
	 *
	 * 2 - orange, ( usually means available room for items, but not types. )
	 *
	 * 3 - red, ( usually means the cell is 100% full )
	 *
	 * @param is      the cell item. ( use the handler for any details you can )
	 * @param handler the handler for the cell is provides for reference, you can cast this to your handler.
	 *
	 * @return get the status of the cell based on its contents.
	 */
	int getStatusForCell( ItemStack is, IMEInventory handler );

	/**
	 * @return the ae/t to drain for this storage cell inside a chest/drive.
	 */
	double cellIdleDrain( ItemStack is, IMEInventory handler );
}