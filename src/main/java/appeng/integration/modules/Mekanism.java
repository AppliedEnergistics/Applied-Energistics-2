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

package appeng.integration.modules;


import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IMekanism;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


public final class Mekanism implements IMekanism, IIntegrationModule
{
	@Reflected
	public static Mekanism instance;

	@Reflected
	public Mekanism()
	{
		IntegrationHelper.testClassExistence( this, mekanism.api.energy.IStrictEnergyAcceptor.class );
	}

	@Override
	public void init() throws Throwable
	{
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public void addCrusherRecipe( final ItemStack in, final ItemStack out )
	{
		final NBTTagCompound sendTag = this.convertToSimpleRecipe( in, out );

		FMLInterModComms.sendMessage( "mekanism", "CrusherRecipe", sendTag );
	}

	@Override
	public void addEnrichmentChamberRecipe( final ItemStack in, final ItemStack out )
	{
		final NBTTagCompound sendTag = this.convertToSimpleRecipe( in, out );

		FMLInterModComms.sendMessage( "mekanism", "EnrichmentChamberRecipe", sendTag );
	}

	private NBTTagCompound convertToSimpleRecipe( final ItemStack in, final ItemStack out )
	{
		final NBTTagCompound sendTag = new NBTTagCompound();
		final NBTTagCompound inputTagDummy = new NBTTagCompound();
		final NBTTagCompound outputTagDummy = new NBTTagCompound();

		final NBTTagCompound inputTag = in.writeToNBT( inputTagDummy );
		final NBTTagCompound outputTag = out.writeToNBT( outputTagDummy );

		sendTag.setTag( "input", inputTag );
		sendTag.setTag( "output", outputTag );

		return sendTag;
	}
}
