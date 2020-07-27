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

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Tickable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import appeng.core.AppEng;
import appeng.tile.AEBaseBlockEntity;
import appeng.util.Platform;

public class CubeGeneratorBlockEntity extends AEBaseBlockEntity implements Tickable {

    private int size = 3;
    private ItemStack is = ItemStack.EMPTY;
    private int countdown = 20 * 10;

    public CubeGeneratorBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void tick() {
        if (!this.is.isEmpty() && Platform.isServer()) {
            this.countdown--;

            if (this.countdown % 20 == 0) {
                AppEng.instance().getPlayers().forEach(e -> {
                    e.sendSystemMessage(new LiteralText("Spawning in... " + (this.countdown / 20)), Util.NIL_UUID);
                });
            }

            if (this.countdown <= 0) {
                this.spawn();
            }
        }
    }

    private void spawn() {
        this.world.removeBlock(this.pos, false);

        final Item i = this.is.getItem();
        final Direction side = Direction.UP;

        final int half = (int) Math.floor(this.size / 2);

        for (int y = 0; y < this.size; y++) {
            for (int x = -half; x < half; x++) {
                for (int z = -half; z < half; z++) {
                    final BlockPos p = this.pos.add(x, y - 1, z);
                    ItemUsageContext useContext = new AutomaticItemPlacementContext(this.world, p, side, this.is,
                            side.getOpposite());
                    i.useOnBlock(useContext);
                }
            }
        }
    }

    void click(final PlayerEntity player) {
        if (Platform.isServer()) {
            final ItemStack hand = player.inventory.getMainHandStack();

            if (hand.isEmpty()) {
                this.is = ItemStack.EMPTY;

                if (player.isInSneakingPose()) {
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

                player.sendSystemMessage(new LiteralText("Size: " + this.size), Util.NIL_UUID);
            } else {
                this.countdown = 20 * 10;
                this.is = hand;
            }
        }
    }
}
