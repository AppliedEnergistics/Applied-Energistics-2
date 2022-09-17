/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.debug;


import appeng.core.AppEng;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;


public class TileCubeGenerator extends AEBaseTile implements ITickable {

    private int size = 3;
    private ItemStack is = ItemStack.EMPTY;
    private int countdown = 20 * 10;
    private EntityPlayer who = null;

    @Override
    public void update() {
        if (!this.is.isEmpty() && Platform.isServer()) {
            this.countdown--;

            if (this.countdown % 20 == 0) {
                for (final EntityPlayer e : AppEng.proxy.getPlayers()) {
                    e.sendMessage(new TextComponentString("Spawning in... " + (this.countdown / 20)));
                }
            }

            if (this.countdown <= 0) {
                this.spawn();
            }
        }
    }

    private void spawn() {
        this.world.setBlockToAir(this.pos);

        final Item i = this.is.getItem();
        final EnumFacing side = EnumFacing.UP;

        final int half = (int) Math.floor(this.size / 2);

        for (int y = 0; y < this.size; y++) {
            for (int x = -half; x < half; x++) {
                for (int z = -half; z < half; z++) {
                    final BlockPos p = this.pos.add(x, y - 1, z);
                    i.onItemUse(this.who, this.world, p, EnumHand.MAIN_HAND, side, 0.5f, 0.0f, 0.5f);
                }
            }
        }
    }

    void click(final EntityPlayer player) {
        if (Platform.isServer()) {
            final ItemStack hand = player.inventory.getCurrentItem();
            this.who = player;

            if (hand.isEmpty()) {
                this.is = ItemStack.EMPTY;

                if (player.isSneaking()) {
                    this.size--;
                } else {
                    this.size++;
                }

                if (this.size < 3) {
                    this.size = 3;
                }
                if (this.size > 64) {
                    this.size = 64;
                }

                player.sendMessage(new TextComponentString("Size: " + this.size));
            } else {
                this.countdown = 20 * 10;
                this.is = hand;
            }
        }
    }
}
