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

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
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

    private final IPartItem<CableAnchorPart> partItem;
    private IPartHost host = null;
    private Direction mySide = Direction.UP;

    public CableAnchorPart(IPartItem<CableAnchorPart> partItem) {
        this.partItem = partItem;
    }

    @Override
    public IPartItem<?> getPartItem() {
        return partItem;
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        if (this.host != null && this.host.getFacadeContainer().getFacade(this.mySide) != null) {
            bch.addBox(7, 7, 10, 9, 9, 14);
        } else {
            bch.addBox(7, 7, 10, 9, 9, 16);
        }
    }

    @Override
    public boolean isLadder(LivingEntity entity) {
        return this.mySide.getStepY() == 0 && (entity.horizontalCollision || !entity.isOnGround());
    }

    @Override
    public IGridNode getGridNode() {
        return null;
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        this.host = host;
        this.mySide = side;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 0;
    }

    @Override
    public boolean canBePlacedOn(BusSupport what) {
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
