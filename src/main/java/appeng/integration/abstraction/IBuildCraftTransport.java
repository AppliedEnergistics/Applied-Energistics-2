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

package appeng.integration.abstraction;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import appeng.api.parts.IFacadePart;
import appeng.api.util.AEPartLocation;
import appeng.client.texture.IAESprite;


/**
 * Contains facade logic to interchange BC facades with AE facades,
 *
 * pipe logic to interact between storage buses and pipes
 *
 * and using pipes for attunements
 * The attunement is currently not public anymore,
 * because it was only used internally
 *
 * @author thatsIch
 * @version rv3 - 12.06.2015
 * @since rv3 12.06.2015
 */
public interface IBuildCraftTransport
{
	/**
	 * @param is to be checked item
	 *
	 * @return {@code true} if the checked item is a {@link buildcraft.api.facades.IFacadeItem}
	 */
	boolean isFacade( @Nullable ItemStack is );

	/**
	 * @param blk block used for the ae facade
	 * @param meta meta of the block
	 * @param side side of the ae facade
	 *
	 * @return ae facade through bc facade system
	 */
	@Nullable
	IFacadePart createFacadePart( @Nullable IBlockState blk, @Nonnull AEPartLocation side );

	/**
	 * @param held create facade for that item
	 * @param side on which side should the part be rendered, should rather be not {@code null}
	 *
	 * @return new instance using the {@code held} and side as direct argument, no logic in between
	 *
	 * @throws IllegalArgumentException if {@code held} is {@code null}
	 */
	IFacadePart createFacadePart( @Nonnull ItemStack held, @Nonnull AEPartLocation side );

	/**
	 * @param facade buildcraft facade
	 *
	 * @return item with the block and metadata based on the facade or {@code null} if {@code facade} was not a facade
	 *
	 * @throws NullPointerException if {@code facade} is {@code null}
	 */
	@Nullable
	ItemStack getTextureForFacade( @Nonnull ItemStack facade );

	/**
	 * @return texture of buildcraft cobblestone structure pipe or null if something bad happens
//	 */
	@Nullable
	IAESprite getCobbleStructurePipeTexture();

	/**
	 * @param te  the to be checked {@link TileEntity}
	 * @param dir direction of the {@link TileEntity}
	 *
	 * @return {@code true} if {@code te} is a buildcraft pipe, but not plugged
	 *
	 * @throws NullPointerException if {@code dir} is {@code null}
	 */
	boolean isPipe( @Nullable TileEntity te, @Nonnull EnumFacing dir );

	/**
	 * checks weather if the {@code te} is injectable and simulates to inject the item
	 *
	 * @param te preferred something like a buildcraft injectable, can handle anything, just fails that way
	 * @param is to be injected item
	 * @param dir direction of the pipe
	 *
	 * @return {@code true} if items were simulated successfully being added
	 */
	boolean canAddItemsToPipe( TileEntity te, ItemStack is, EnumFacing dir );

	/**
	 * checks weather if the {@code te} is injectable, simulates the inject and tries to inject the item
	 *
	 * @param te preferred something like a buildcraft injectable, can handle anything, just fails that way
	 * @param is to be injected item
	 * @param dir direction of the pipe
	 *
	 * @return {@code true} if items were added to the buildcraft pipe
	 */
	boolean addItemsToPipe( @Nullable TileEntity te, @Nullable ItemStack is, @Nonnull EnumFacing dir );
}
