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

package appeng.integration.modules.BCHelpers;


import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.blueprints.IBuilderContext;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.parts.CableBusContainer;


public final class AECableSchematicTile extends AEGenericSchematicTile implements IPartHost
{

	@Override
	public final void rotateLeft( IBuilderContext context )
	{
		CableBusContainer cbc = new CableBusContainer( this );
		cbc.readFromNBT( this.tileNBT );

		cbc.rotateLeft();

		this.tileNBT = new NBTTagCompound();
		cbc.writeToNBT( this.tileNBT );
	}

	@Override
	public final IFacadeContainer getFacadeContainer()
	{
		return null;
	}

	@Override
	public final boolean canAddPart( ItemStack part, ForgeDirection side )
	{
		return false;
	}

	@Override
	public final ForgeDirection addPart( ItemStack is, ForgeDirection side, EntityPlayer owner )
	{
		return null;
	}

	@Override
	public final IPart getPart( ForgeDirection side )
	{
		return null;
	}

	@Override
	public final void removePart( ForgeDirection side, boolean suppressUpdate )
	{

	}

	@Override
	public final void markForUpdate()
	{

	}

	@Override
	public final DimensionalCoord getLocation()
	{
		return null;
	}

	@Override
	public final TileEntity getTile()
	{
		return null;
	}

	@Override
	public final AEColor getColor()
	{
		return null;
	}

	@Override
	public final void clearContainer()
	{

	}

	@Override
	public final boolean isBlocked( ForgeDirection side )
	{
		return false;
	}

	@Override
	public final SelectedPart selectPart( Vec3 pos )
	{
		return null;
	}

	@Override
	public final void markForSave()
	{

	}

	@Override
	public final void partChanged()
	{

	}

	@Override
	public final boolean hasRedstone( ForgeDirection side )
	{
		return false;
	}

	@Override
	public final boolean isEmpty()
	{
		return false;
	}

	@Override
	public final Set<LayerFlags> getLayerFlags()
	{
		return null;
	}

	@Override
	public final void cleanup()
	{

	}

	@Override
	public final void notifyNeighbors()
	{

	}

	@Override
	public final boolean isInWorld()
	{
		return false;
	}
}
