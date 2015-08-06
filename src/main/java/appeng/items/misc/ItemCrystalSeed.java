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


import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.EntityRegistry;

import appeng.api.AEApi;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.recipes.ResolverResult;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.localization.ButtonToolTips;
import appeng.entity.EntityGrowingCrystal;
import appeng.entity.EntityIds;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ItemCrystalSeed extends AEBaseItem implements IGrowableCrystal
{

	public static final int LEVEL_OFFSET = 200;
	public static final int SINGLE_OFFSET = LEVEL_OFFSET * 3;

	public static final int CERTUS = 0;
	public static final int NETHER = SINGLE_OFFSET;
	public static final int FLUIX = SINGLE_OFFSET * 2;
	public static final int FINAL_STAGE = SINGLE_OFFSET * 3;

	final IIcon[] certus = new IIcon[3];
	final IIcon[] fluix = new IIcon[3];
	final IIcon[] nether = new IIcon[3];

	public ItemCrystalSeed()
	{
		this.setHasSubtypes( true );
		this.setFeature( EnumSet.of( AEFeature.Core ) );

		EntityRegistry.registerModEntity( EntityGrowingCrystal.class, EntityGrowingCrystal.class.getSimpleName(), EntityIds.get( EntityGrowingCrystal.class ), AppEng.instance(), 16, 4, true );
	}

	@Nullable
	public static ResolverResult getResolver( int certus2 )
	{
		ResolverResult resolver = null;

		for( ItemStack crystalSeedStack : AEApi.instance().definitions().items().crystalSeed().maybeStack( 1 ).asSet() )
		{
			crystalSeedStack.setItemDamage( certus2 );
			crystalSeedStack = newStyle( crystalSeedStack );
			resolver = new ResolverResult( "ItemCrystalSeed", crystalSeedStack.getItemDamage(), crystalSeedStack.getTagCompound() );
		}

		return resolver;
	}

	private static ItemStack newStyle( ItemStack itemStack )
	{
		( (ItemCrystalSeed) itemStack.getItem() ).getProgress( itemStack );
		return itemStack;
	}

	private int getProgress( ItemStack is )
	{
		if( is.hasTagCompound() )
		{
			return is.getTagCompound().getInteger( "progress" );
		}
		else
		{
			int progress;
			NBTTagCompound comp = Platform.openNbtData( is );
			comp.setInteger( "progress", progress = is.getItemDamage() );
			is.setItemDamage( ( is.getItemDamage() / SINGLE_OFFSET ) * SINGLE_OFFSET );
			return progress;
		}
	}

	@Nullable
	@Override
	public ItemStack triggerGrowth( ItemStack is )
	{
		int newDamage = this.getProgress( is ) + 1;
		final IMaterials materials = AEApi.instance().definitions().materials();
		final int size = is.stackSize;

		if( newDamage == CERTUS + SINGLE_OFFSET )
		{
			for( ItemStack quartzStack : materials.purifiedCertusQuartzCrystal().maybeStack( size ).asSet() )
			{
				return quartzStack;
			}
		}
		if( newDamage == NETHER + SINGLE_OFFSET )
		{
			for( ItemStack quartzStack : materials.purifiedNetherQuartzCrystal().maybeStack( size ).asSet() )
			{
				return quartzStack;
			}
		}
		if( newDamage == FLUIX + SINGLE_OFFSET )
		{
			for( ItemStack quartzStack : materials.purifiedFluixCrystal().maybeStack( size ).asSet() )
			{
				return quartzStack;
			}
		}
		if( newDamage > FINAL_STAGE )
		{
			return null;
		}

		this.setProgress( is, newDamage );
		return is;
	}

	private void setProgress( ItemStack is, int newDamage )
	{
		NBTTagCompound comp = Platform.openNbtData( is );
		comp.setInteger( "progress", newDamage );
		is.setItemDamage( is.getItemDamage() / LEVEL_OFFSET * LEVEL_OFFSET );
	}

	@Override
	public float getMultiplier( Block blk, Material mat )
	{
		return 0.5f;
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayMoreInfo )
	{
		lines.add( ButtonToolTips.DoesntDespawn.getLocal() );
		int progress = this.getProgress( stack ) % SINGLE_OFFSET;
		lines.add( Math.floor( (float) progress / (float) ( SINGLE_OFFSET / 100 ) ) + "%" );

		super.addCheckedInformation( stack, player, lines, displayMoreInfo );
	}

	@Override
	public int getEntityLifespan( ItemStack itemStack, World world )
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public String getUnlocalizedName( ItemStack is )
	{
		int damage = this.getProgress( is );

		if( damage < CERTUS + SINGLE_OFFSET )
		{
			return this.getUnlocalizedName() + ".Certus";
		}

		if( damage < NETHER + SINGLE_OFFSET )
		{
			return this.getUnlocalizedName() + ".Nether";
		}

		if( damage < FLUIX + SINGLE_OFFSET )
		{
			return this.getUnlocalizedName() + ".Fluix";
		}

		return this.getUnlocalizedName();
	}

	@Override
	public boolean isDamageable()
	{
		return false;
	}

	@Override
	public boolean isDamaged( ItemStack stack )
	{
		return false;
	}

	@Override
	public int getMaxDamage( ItemStack stack )
	{
		return FINAL_STAGE;
	}

	@Override
	public IIcon getIcon( ItemStack stack, int pass )
	{
		return this.getIconIndex( stack );
	}

	@Override
	public IIcon getIconIndex( ItemStack stack )
	{
		IIcon[] list = null;

		int damage = this.getProgress( stack );

		if( damage < CERTUS + SINGLE_OFFSET )
		{
			list = this.certus;
		}
		else if( damage < NETHER + SINGLE_OFFSET )
		{
			damage -= NETHER;
			list = this.nether;
		}

		else if( damage < FLUIX + SINGLE_OFFSET )
		{
			damage -= FLUIX;
			list = this.fluix;
		}

		if( list == null )
		{
			return Items.diamond.getIconFromDamage( 0 );
		}

		if( damage < LEVEL_OFFSET )
		{
			return list[0];
		}
		else if( damage < LEVEL_OFFSET * 2 )
		{
			return list[1];
		}
		else
		{
			return list[2];
		}
	}

	@Override
	public void registerIcons( IIconRegister ir )
	{
		String preFix = "appliedenergistics2:ItemCrystalSeed.";

		this.certus[0] = ir.registerIcon( preFix + "Certus" );
		this.certus[1] = ir.registerIcon( preFix + "Certus2" );
		this.certus[2] = ir.registerIcon( preFix + "Certus3" );

		this.nether[0] = ir.registerIcon( preFix + "Nether" );
		this.nether[1] = ir.registerIcon( preFix + "Nether2" );
		this.nether[2] = ir.registerIcon( preFix + "Nether3" );

		this.fluix[0] = ir.registerIcon( preFix + "Fluix" );
		this.fluix[1] = ir.registerIcon( preFix + "Fluix2" );
		this.fluix[2] = ir.registerIcon( preFix + "Fluix3" );
	}

	@Override
	public boolean hasCustomEntity( ItemStack stack )
	{
		return true;
	}

	@Override
	public Entity createEntity( World world, Entity location, ItemStack itemstack )
	{
		EntityGrowingCrystal egc = new EntityGrowingCrystal( world, location.posX, location.posY, location.posZ, itemstack );

		egc.motionX = location.motionX;
		egc.motionY = location.motionY;
		egc.motionZ = location.motionZ;

		if( location instanceof EntityItem )
		{
			egc.delayBeforeCanPickup = ( (EntityItem) location ).delayBeforeCanPickup;
		}

		return egc;
	}

	@Override
	protected void getCheckedSubItems( Item sameItem, CreativeTabs creativeTab, List<ItemStack> itemStacks )
	{
		// lvl 0
		itemStacks.add( newStyle( new ItemStack( this, 1, CERTUS ) ) );
		itemStacks.add( newStyle( new ItemStack( this, 1, NETHER ) ) );
		itemStacks.add( newStyle( new ItemStack( this, 1, FLUIX ) ) );

		// lvl 1
		itemStacks.add( newStyle( new ItemStack( this, 1, LEVEL_OFFSET + CERTUS ) ) );
		itemStacks.add( newStyle( new ItemStack( this, 1, LEVEL_OFFSET + NETHER ) ) );
		itemStacks.add( newStyle( new ItemStack( this, 1, LEVEL_OFFSET + FLUIX ) ) );

		// lvl 2
		itemStacks.add( newStyle( new ItemStack( this, 1, LEVEL_OFFSET * 2 + CERTUS ) ) );
		itemStacks.add( newStyle( new ItemStack( this, 1, LEVEL_OFFSET * 2 + NETHER ) ) );
		itemStacks.add( newStyle( new ItemStack( this, 1, LEVEL_OFFSET * 2 + FLUIX ) ) );
	}
}
