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

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import appeng.tile.ServerTickingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;

import appeng.core.AppEng;
import appeng.tile.AEBaseTileEntity;
import appeng.util.InteractionUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public class CubeGeneratorTileEntity extends AEBaseTileEntity implements ServerTickingBlockEntity {

    private int size = 3;
    private ItemStack is = ItemStack.EMPTY;
    private int countdown = 20 * 10;
    private Player who = null;

    public CubeGeneratorTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
    }

    @Override
    public void serverTick() {
        if (!this.is.isEmpty()) {
            this.countdown--;

            if (this.countdown % 20 == 0) {
                AppEng.instance().getPlayers().forEach(p -> {
                    p.sendMessage(new TextComponent("Spawning in... " + this.countdown / 20), Util.NIL_UUID);
                });
            }

            if (this.countdown <= 0) {
                this.spawn();
            }
        }
    }

    private void spawn() {
        this.level.removeBlock(this.worldPosition, false);

        final Item i = this.is.getItem();
        final Direction side = Direction.UP;

        final int half = (int) Math.floor(this.size / 2);

        for (int y = 0; y < this.size; y++) {
            for (int x = -half; x < half; x++) {
                for (int z = -half; z < half; z++) {
                    final BlockPos p = this.worldPosition.offset(x, y - 1, z);
                    UseOnContext useContext = new DirectionalPlaceContext(this.level, p, side, this.is,
                            side.getOpposite());
                    i.useOn(useContext);
                }
            }
        }
    }

    void click(final Player player) {
        if (!isRemote()) {
            final ItemStack hand = player.inventory.getSelected();
            this.who = player;

            if (hand.isEmpty()) {
                this.is = ItemStack.EMPTY;

                if (InteractionUtil.isInAlternateUseMode(player)) {
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

                player.sendMessage(new TextComponent("Size: " + this.size), Util.NIL_UUID);
            } else {
                this.countdown = 20 * 10;
                this.is = hand;
            }
        }
    }
}
