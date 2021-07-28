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

package appeng.parts.misc;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;

public class CableAnchorPart implements IPart {

    @PartModels
    public static final PartModel DEFAULT_MODELS = new PartModel(false,
            new ResourceLocation(AppEng.MOD_ID, "part/cable_anchor"));

    @PartModels
    public static final PartModel FACADE_MODELS = new PartModel(false,
            new ResourceLocation(AppEng.MOD_ID, "part/cable_anchor_short"));

    private ItemStack is = ItemStack.EMPTY;
    private IPartHost host = null;
    private AEPartLocation mySide = AEPartLocation.UP;

    public CableAnchorPart(final ItemStack is) {
        this.is = is;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        if (this.host != null && this.host.getFacadeContainer().getFacade(this.mySide) != null) {
            bch.addBox(7, 7, 10, 9, 9, 14);
        } else {
            bch.addBox(7, 7, 10, 9, 9, 16);
        }
    }

    @Override
    public ItemStack getItemStack(final PartItemStack wrenched) {
        return this.is;
    }

    @Override
    public boolean requireDynamicRender() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canConnectRedstone() {
        return false;
    }

    @Override
    public void writeToNBT(final CompoundTag data) {

    }

    @Override
    public void readFromNBT(final CompoundTag data) {

    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public boolean isLadder(final LivingEntity entity) {
        return this.mySide.yOffset == 0 && (entity.horizontalCollision || !entity.isOnGround());
    }

    @Override
    public void onNeighborChanged(BlockGetter w, BlockPos pos, BlockPos neighbor) {

    }

    @Override
    public int isProvidingStrongPower() {
        return 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return 0;
    }

    @Override
    public void writeToStream(final FriendlyByteBuf data) throws IOException {

    }

    @Override
    public boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        return false;
    }

    @Override
    public IGridNode getGridNode() {
        return null;
    }

    @Override
    public void onEntityCollision(final Entity entity) {

    }

    @Override
    public void removeFromWorld() {

    }

    @Override
    public void addToWorld() {

    }

    @Override
    public IGridNode getExternalFacingNode() {
        return null;
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final BlockEntity tile) {
        this.host = host;
        this.mySide = side;
    }

    @Override
    public boolean onActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        return false;
    }

    @Override
    public boolean onShiftActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        return false;
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {

    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 0;
    }

    @Override
    public void animateTick(final Level world, final BlockPos pos, final Random r) {

    }

    @Override
    public void onPlacement(final Player player, final InteractionHand hand, final ItemStack held,
            final AEPartLocation side) {

    }

    @Override
    public boolean canBePlacedOn(final BusSupport what) {
        return what == BusSupport.CABLE || what == BusSupport.DENSE_CABLE;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.host != null && this.host.getFacadeContainer().getFacade(this.mySide) != null) {
            return FACADE_MODELS;
        } else {
            return DEFAULT_MODELS;
        }
    }

}
