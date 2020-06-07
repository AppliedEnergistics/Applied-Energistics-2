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

package appeng.items.misc;


import appeng.api.util.AEColor;
import appeng.items.AEBaseItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;


public class ItemPaintBall extends AEBaseItem
{

	private static final String TAG_COLOR = "c";

	private final boolean lumen;

	public ItemPaintBall(Properties properties, boolean lumen) {
		super(properties);
		this.lumen = lumen;
	}

	@Override
	public ITextComponent getDisplayName(final ItemStack is )
	{
		// FIXME: in the new system, we should have different items for the colors,
		// FIXME: Making this function fully redundant
		return super.getDisplayName( is ).shallowCopy().appendText(" - ").appendSibling(this.getExtraName( is ));
	}

	private ITextComponent getExtraName( final ItemStack is )
	{
		return new TranslationTextComponent(this.getColor(is).translationKey);
	}

	public AEColor getColor( final ItemStack is )
	{
		CompoundNBT tag = is.getTag();
		if (tag == null || !tag.contains(TAG_COLOR)) {
			return AEColor.TRANSPARENT;
		}
		int c = tag.getInt(TAG_COLOR);
		if (c < 0 || c >= AEColor.values().length) {
			return AEColor.TRANSPARENT;
		}
		return AEColor.values()[c];
	}

	public ItemStack setColor( final ItemStack is, AEColor color )
	{
		is.getOrCreateTag().putInt(TAG_COLOR, color.ordinal());
		return is;
	}

	public boolean isLumen()
	{
		return lumen;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> itemStacks) {

		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.TRANSPARENT )
			{
				itemStacks.add( setColor(new ItemStack( this ), c) );
			}
		}
	}

}
