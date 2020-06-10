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

package appeng.items.tools.quartz;


import appeng.core.AppEng;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;

import appeng.core.features.AEFeature;
import appeng.util.Platform;


public class ToolQuartzShovel extends ShovelItem
{
	private final AEFeature type;

	public ToolQuartzShovel( final AEFeature type )
	{
		// A real nice way of creating an Iron tool without hard coding its values - Yay!
		super( ItemTier.IRON,
				(int) ( Items.IRON_SHOVEL.getAttributeModifiers( EquipmentSlotType.MAINHAND )
						.get( SharedMonsterAttributes.ATTACK_DAMAGE.getName() ).stream().filter( am -> am.getName().equals( "Tool modifier" ) ).findFirst().get().getAmount() - ItemTier.IRON.getAttackDamage()),
				(float) Items.IRON_SHOVEL.getAttributeModifiers( EquipmentSlotType.MAINHAND )
						.get( SharedMonsterAttributes.ATTACK_SPEED.getName() ).stream().filter( am -> am.getName().equals( "Tool modifier" ) ).findFirst().get().getAmount(),
				(new Item.Properties()).group( AppEng.ITEM_GROUP ));
		this.type = type;
	}

	@Override
	public boolean getIsRepairable( final ItemStack a, final ItemStack b )
	{
		return Platform.canRepair( this.type, a, b );
	}
}
