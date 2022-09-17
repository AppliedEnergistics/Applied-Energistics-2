/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.tile.storage;


import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;


public class TileSkyChest extends AEBaseInvTile implements ITickable {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);
    // server
    private int numPlayersUsing;
    // client..
    private long lastEvent;
    private float lidAngle;
    private float prevLidAngle;

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getPlayerOpen() > 0);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int wasOpen = this.getPlayerOpen();
        this.setPlayerOpen(data.readBoolean() ? 1 : 0);

        if (wasOpen != this.getPlayerOpen()) {
            this.setLastEvent(System.currentTimeMillis());
        }

        return c; // TESR yo!
    }

    @Override
    public boolean requiresTESR() {
        return true;
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    public void openInventory(final EntityPlayer player) {
        if (!player.isSpectator()) {
            this.setPlayerOpen(this.getPlayerOpen() + 1);
            this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), true);
            this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType(), true);

            if (this.getPlayerOpen() == 1) {
                this.getWorld()
                        .playSound(player, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_OPEN,
                                SoundCategory.BLOCKS, 0.5F, this.getWorld().rand.nextFloat() * 0.1F + 0.9F);
                this.markForUpdate();
            }
        }
    }

    public void closeInventory(final EntityPlayer player) {
        if (!player.isSpectator()) {
            this.setPlayerOpen(this.getPlayerOpen() - 1);
            this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
            this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), true);
            this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType(), true);

            if (this.getPlayerOpen() < 0) {
                this.setPlayerOpen(0);
            }

            if (this.getPlayerOpen() == 0) {
                this.getWorld()
                        .playSound(player, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, SoundEvents.BLOCK_CHEST_CLOSE,
                                SoundCategory.BLOCKS, 0.5F, this.getWorld().rand.nextFloat() * 0.1F + 0.9F);
                this.markForUpdate();
            }
        }
    }

    @Override
    public void update() {
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();

        this.prevLidAngle = this.lidAngle;
        float f1 = 0.1F;

        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float f2 = this.lidAngle;

            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            float f3 = 0.5F;

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {

    }

    public float getLidAngle() {
        return this.lidAngle;
    }

    public void setLidAngle(final float lidAngle) {
        this.lidAngle = lidAngle;
    }

    public float getPrevLidAngle() {
        return this.prevLidAngle;
    }

    public void setPrevLidAngle(float prevLidAngle) {
        this.prevLidAngle = prevLidAngle;
    }

    public int getPlayerOpen() {
        return this.numPlayersUsing;
    }

    private void setPlayerOpen(final int playerOpen) {
        this.numPlayersUsing = playerOpen;
    }

    public long getLastEvent() {
        return this.lastEvent;
    }

    private void setLastEvent(final long lastEvent) {
        this.lastEvent = lastEvent;
    }
}
