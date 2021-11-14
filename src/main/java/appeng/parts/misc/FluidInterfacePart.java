/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.helpers.DualityFluidInterface;
import appeng.helpers.IFluidInterfaceHost;
import appeng.items.parts.PartModels;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.parts.AEBasePart;
import appeng.parts.BasicStatePart;
import appeng.parts.PartModel;

public class FluidInterfacePart extends BasicStatePart implements IFluidInterfaceHost {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_base");

    private static final IGridNodeListener<FluidInterfacePart> NODE_LISTENER = new AEBasePart.NodeListener<>() {
        @Override
        public void onGridChanged(FluidInterfacePart nodeOwner, IGridNode node) {
            super.onGridChanged(nodeOwner, node);
            nodeOwner.duality.gridChanged();
        }
    };

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_interface_has_channel"));

    private final DualityFluidInterface duality;

    public FluidInterfacePart(final ItemStack is) {
        super(is);
        this.duality = new DualityFluidInterface(this.getMainNode(), this, is);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public DualityFluidInterface getInterfaceDuality() {
        return this.duality;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.duality.notifyNeighbors();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.duality.readFromNBT(data);
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.duality.writeToNBT(data);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public boolean onPartActivate(final Player p, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(getMenuType(), p, MenuLocator.forPart(this));
        }

        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(UPGRADES)) {
            return duality.getUpgrades();
        }
        return super.getSubInventory(id);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.FLUID_INTERFACE.stack();
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }
}
