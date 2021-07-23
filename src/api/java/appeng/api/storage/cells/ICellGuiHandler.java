/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

package appeng.api.storage.cells;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICellGuiHandler {
    /**
     * Return true if this handler can show GUI for this channel.
     *
     * @param channel Storage channel
     * @return True if handled, else false.
     */
    <T extends IAEStack<T>> boolean isHandlerFor(IStorageChannel<T> channel);

    /**
     * Return true to prioritize this handler for the provided {@link ItemStack}.
     *
     * @param is Cell ItemStack
     * @return True, if specialized else false.
     */
    default boolean isSpecializedFor(net.minecraft.world.item.ItemStack is) {
        return false;
    }

    /**
     * Called when the storage cell is placed in an ME Chest and the user tries to open the terminal side, if your item
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
    <T extends IAEStack<T>> void openChestGui(Player player, IChestOrDrive chest, ICellHandler cellHandler,
                                              IMEInventoryHandler<T> inv, net.minecraft.world.item.ItemStack is, IStorageChannel<T> chan);

}
