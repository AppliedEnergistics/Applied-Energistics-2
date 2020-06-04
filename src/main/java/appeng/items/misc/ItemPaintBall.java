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


import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import appeng.api.util.AEColor;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;


public class ItemPaintBall extends AEBaseItem
{

	private static final ITextComponent LUMEN_PREFIX = new TranslationTextComponent(GuiText.Lumen.getTranslationKey())
			.appendText(" ");

	private static final int DAMAGE_THRESHOLD = 20;

	public ItemPaintBall(Properties properties) {
		super(properties);
		// FIXME this.setHasSubtypes( true );
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
		ITextComponent colorText = new TranslationTextComponent(this.getColor(is).translationKey);
		if (is.getDamage() >= DAMAGE_THRESHOLD) {
			return LUMEN_PREFIX.shallowCopy().appendSibling(colorText);
		} else {
			return colorText;
		}
	}

	public AEColor getColor( final ItemStack is )
	{
		int dmg = is.getDamage();
		if( dmg >= DAMAGE_THRESHOLD )
		{
			dmg -= DAMAGE_THRESHOLD;
		}

		if( dmg >= AEColor.values().length )
		{
			return AEColor.TRANSPARENT;
		}

		return AEColor.values()[dmg];
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> itemStacks) {

		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.TRANSPARENT )
			{
				itemStacks.add( new ItemStack( this, 1 /* FIXME, c.ordinal() */ ) );
			}
		}

		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.TRANSPARENT )
			{
				itemStacks.add( new ItemStack( this, 1 /* FIXME , DAMAGE_THRESHOLD + c.ordinal() */ ) );
			}
		}
	}

	public static boolean isLumen( final ItemStack is )
	{
		final int dmg = is.getDamage();
		return dmg >= DAMAGE_THRESHOLD;
	}

}
