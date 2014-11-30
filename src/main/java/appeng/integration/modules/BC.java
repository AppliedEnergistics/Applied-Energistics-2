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


import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.event.FMLInterModComms;

import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftTransport;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TileGenericPipe;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.Blocks;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.parts.IFacadePart;
import appeng.api.util.AEItemDefinition;
import appeng.api.util.IOrientableBlock;
import appeng.core.AppEng;
import appeng.facade.FacadePart;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IBC;
import appeng.integration.modules.BCHelpers.AECableSchematicTile;
import appeng.integration.modules.BCHelpers.AEGenericSchematicTile;
import appeng.integration.modules.BCHelpers.AERotatableBlockSchematic;
import appeng.integration.modules.BCHelpers.BCPipeHandler;

public class BC extends BaseModule implements IBC
{

	public static BC instance;

	public BC() {
		TestClass( IPipeConnection.class );
		TestClass( ItemFacade.class );
		TestClass( IToolWrench.class );
	}

	@Override
	public void addFacade(ItemStack item)
	{
		if ( item != null )
			FMLInterModComms.sendMessage( "BuildCraft|Transport", "add-facade", item );
	}

	@Override
	public boolean isWrench(Item eq)
	{
		return eq instanceof IToolWrench;
	}

	@Override
	public boolean isPipe(TileEntity te, ForgeDirection dir)
	{
		if ( te instanceof IPipeTile )
		{
			try
			{
				if ( te instanceof TileGenericPipe )
					if ( ((TileGenericPipe) te).hasPlug( dir.getOpposite() ) )
						return false;
			}
			catch (Exception ignored)
			{
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean canWrench(Item i, EntityPlayer p, int x, int y, int z)
	{
		return ((IToolWrench) i).canWrench( p, x, y, z );
	}

	@Override
	public void wrenchUsed(Item i, EntityPlayer p, int x, int y, int z)
	{
		((IToolWrench) i).wrenchUsed( p, x, y, z );
	}

	@Override
	public boolean addItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir)
	{
		if ( is != null && te != null && te instanceof IPipeTile )
		{
			IPipeTile pt = (IPipeTile) te;
			if ( pt.getPipeType() == PipeType.ITEM )
			{
				int amt = pt.injectItem( is, false, dir );
				if ( amt == is.stackSize )
				{
					pt.injectItem( is, true, dir );
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isFacade(ItemStack is)
	{
		if ( is == null )
			return false;

		try
		{
			return is.getItem() instanceof ItemFacade && ItemFacade.getType( is ) == ItemFacade.FacadeType.Basic;
		}
		catch (Throwable t)
		{
			try
			{
				return is.getItem() instanceof ItemFacade && ItemFacade.getType( is ) == ItemFacade.TYPE_BASIC;
			}
			catch (Throwable g)
			{
				return is.getItem() instanceof ItemFacade;
			}
		}
	}

	@Override
	public boolean canAddItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir)
	{

		if ( is != null && te != null && te instanceof IPipeTile )
		{
			IPipeTile pt = (IPipeTile) te;
			if ( pt.getPipeType() == PipeType.ITEM )
			{
				int amt = pt.injectItem( is, false, dir );
				if ( amt == is.stackSize )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void registerPowerP2P()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 0 ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 1 ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 2 ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerCobblestone ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerDiamond ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerGold ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerQuartz ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerStone ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerWood ), TunnelType.BC_POWER );
	}

	@Override
	public void registerItemP2P()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsWood ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsVoid ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsSandstone ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsQuartz ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsObsidian ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsIron ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsGold ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsEmerald ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsDiamond ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsStone ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsCobblestone ), TunnelType.ITEM );
	}

	@Override
	public void registerLiquidsP2P()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsCobblestone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsEmerald ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsGold ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsIron ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsSandstone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsStone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsVoid ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsWood ), TunnelType.FLUID );
	}

	@Override
	public void Init()
	{
		AEApi.instance().partHelper().registerNewLayer( "appeng.parts.layers.LayerIPipeConnection", "buildcraft.api.transport.IPipeConnection" );
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new BCPipeHandler() );

		Blocks b = AEApi.instance().blocks();
		addFacade( b.blockFluix.stack( 1 ) );
		addFacade( b.blockQuartz.stack( 1 ) );
		addFacade( b.blockQuartzChiseled.stack( 1 ) );
		addFacade( b.blockQuartzPillar.stack( 1 ) );

		try
		{
			initBuilderSupport();
		}
		catch (Throwable builderSupport)
		{
			// not supported?
		}

		Block skyStone = b.blockSkyStone.block();
		if ( skyStone != null )
		{
			addFacade( new ItemStack( skyStone, 1, 0 ) );
			addFacade( new ItemStack( skyStone, 1, 1 ) );
			addFacade( new ItemStack( skyStone, 1, 2 ) );
			addFacade( new ItemStack( skyStone, 1, 3 ) );
		}
	}

	private void initBuilderSupport()
	{
		SchematicRegistry.declareBlueprintSupport( AppEng.MOD_ID );

		Blocks blocks = AEApi.instance().blocks();
		Block cable = blocks.blockMultiPart.block();
		for (Field f : blocks.getClass().getFields())
		{
			AEItemDefinition def;
			try
			{
				def = (AEItemDefinition) f.get( blocks );
				if ( def != null )
				{
					Block myBlock = def.block();
					if ( myBlock instanceof IOrientableBlock && ((IOrientableBlock) myBlock).usesMetadata() && def.entity() == null )
					{
						SchematicRegistry.registerSchematicBlock( myBlock, AERotatableBlockSchematic.class );
					}
					else if ( myBlock == cable )
					{
						SchematicRegistry.registerSchematicBlock( myBlock, AECableSchematicTile.class );
					}
					else if ( def.entity() != null )
					{
						SchematicRegistry.registerSchematicBlock( myBlock, AEGenericSchematicTile.class );
					}
				}
			}
			catch (Throwable t)
			{
				// :P
			}
		}
	}

	@Override
	public void PostInit()
	{
		registerPowerP2P();
		registerItemP2P();
		registerLiquidsP2P();
	}

	@Override
	public IFacadePart createFacadePart(Block blk, int meta, ForgeDirection side)
	{
		try
		{
			ItemStack fs = ItemFacade.getFacade( blk, meta );
			return new FacadePart( fs, side );
		}
		catch (Throwable ignored)
		{

		}

		try
		{
			ItemStack fs = ItemFacade.getStack( blk, meta );
			return new FacadePart( fs, side );
		}
		catch (Throwable ignored)
		{

		}

		return null;
	}

	@Override
	public IFacadePart createFacadePart(ItemStack fs, ForgeDirection side)
	{
		return new FacadePart( fs, side );
	}

	@Override
	public ItemStack getTextureForFacade(ItemStack facade)
	{
		try
		{
			Block blk[] = ItemFacade.getBlocks( facade );
			int meta[] = ItemFacade.getMetaValues( facade );
			if ( blk == null || blk.length < 1 )
				return null;

			if ( blk[0] != null )
				return new ItemStack( blk[0], 1, meta[0] );
		}
		catch (Throwable ignored)
		{

		}

		try
		{
			Block blk = ItemFacade.getBlock( facade );
			if ( blk != null )
				return new ItemStack( blk, 1, ItemFacade.getMetaData( facade ) );
		}
		catch (Throwable ignored)
		{

		}

		return null;
	}

	@Override
	public IIcon getFacadeTexture()
	{
		try
		{
			return BuildCraftTransport.instance.pipeIconProvider.getIcon( PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal() ); // Structure
		}
		catch (Throwable ignored)
		{
		}
		return null;
		// Pipe
	}

}
