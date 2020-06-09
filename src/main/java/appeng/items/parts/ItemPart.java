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

package appeng.items.parts;


import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.core.features.ActivityState;
import appeng.core.features.ItemStackSrc;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;


public class ItemPart<T extends IPart> extends AEBaseItem implements IPartItem<T>, IItemGroup
{
	private final PartType type;

	private final Function<ItemStack, T> factory;

	public ItemPart(Properties properties, PartType type, Function<ItemStack, T> factory) {
		super(properties);
		this.type = type;
		this.factory = factory;
	}

	@Override
	public ActionResultType onItemUse( ItemUseContext context )
	{
		PlayerEntity player = context.getPlayer();
		ItemStack held = player.getHeldItem( context.getHand() );
		if( held.getItem() != this )
		{
			return ActionResultType.PASS;
		}

		return AEApi.instance().partHelper().placeBus( held, context.getPos(), context.getFace(), player, context.getHand(), context.getWorld() );
	}

	public PartType getType() {
		return type;
	}

	@Override
	public T createPart(ItemStack is) {
		return factory.apply(is);
	}

	private static PartType getTypeByStack(ItemStack is) {
		if (is.getItem() instanceof ItemPart) {
			return ((ItemPart<?>) is.getItem()).getType();
		}
		return PartType.INVALID_TYPE;
	}

	@Nullable
	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		boolean importBus = false;
		boolean importBusFluids = false;
		boolean exportBus = false;
		boolean exportBusFluids = false;
		boolean group = false;

		final PartType u = getTypeByStack( is );

		for( final ItemStack stack : others )
		{
			if( stack.getItem() == this )
			{
				final PartType pt = getTypeByStack( stack );
				switch( pt )
				{
					case IMPORT_BUS:
						importBus = true;
						if( u == pt )
						{
							group = true;
						}
						break;
					case FLUID_IMPORT_BUS:
						importBusFluids = true;
						if( u == pt )
						{
							group = true;
						}
						break;
					case EXPORT_BUS:
						exportBus = true;
						if( u == pt )
						{
							group = true;
						}
						break;
					case FLUID_EXPORT_BUS:
						exportBusFluids = true;
						if( u == pt )
						{
							group = true;
						}
						break;
					default:
				}
			}
		}

		if( group && importBus && exportBus && ( u == PartType.IMPORT_BUS || u == PartType.EXPORT_BUS ) )
		{
			return GuiText.IOBuses.getTranslationKey();
		}
		if( group && importBusFluids && exportBusFluids && ( u == PartType.FLUID_IMPORT_BUS || u == PartType.FLUID_EXPORT_BUS ) )
		{
			return GuiText.IOBusesFluids.getTranslationKey();
		}

		return null;
	}

}
