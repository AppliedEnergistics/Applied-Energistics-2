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
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.Blocks;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.parts.IFacadePart;
import appeng.api.util.AEItemDefinition;
import appeng.api.util.IOrientableBlock;
import appeng.facade.FacadePart;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IBC;
import appeng.integration.modules.BCHelpers.AECableSchematicTile;
import appeng.integration.modules.BCHelpers.AEGenericSchematicTile;
import appeng.integration.modules.BCHelpers.AERotatableBlockSchematic;
import appeng.integration.modules.BCHelpers.BCPipeHandler;

public final class BC extends BaseModule implements IBC
{

	public static BC instance;

	public BC() {
		this.testClassExistence( IPipeConnection.class );
		this.testClassExistence( ItemFacade.class );
		this.testClassExistence( IToolWrench.class );
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
			final IPipeTile pipeTile = (IPipeTile) te;
			return !pipeTile.hasPipePluggable( dir.getOpposite() );
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
		if ( is != null && te != null && te instanceof IInjectable )
		{
			IInjectable pt = (IInjectable) te;
			if ( pt.canInjectItems( dir ) )
			{
				int amt = pt.injectItem( is, false, dir, null );
				if ( amt == is.stackSize )
				{
					pt.injectItem( is, true, dir, null );
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

		return is.getItem() instanceof IFacadeItem;
	}

	@Override
	public boolean canAddItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir)
	{
		if ( is != null && te != null && te instanceof IInjectable )
		{
			IInjectable pt = (IInjectable) te;
			if ( pt.canInjectItems( dir ) )
			{
				int amt = pt.injectItem( is, false, dir, null );
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
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 0 ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 1 ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 2 ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerCobblestone ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerDiamond ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerGold ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerQuartz ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerStone ), TunnelType.RF_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerWood ), TunnelType.RF_POWER );
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
	public void init()
	{
		AEApi.instance().partHelper().registerNewLayer( "appeng.parts.layers.LayerIPipeConnection", "buildcraft.api.transport.IPipeConnection" );
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new BCPipeHandler() );

		Blocks b = AEApi.instance().blocks();
		this.addFacade( b.blockFluix.stack( 1 ) );
		this.addFacade( b.blockQuartz.stack( 1 ) );
		this.addFacade( b.blockQuartzChiseled.stack( 1 ) );
		this.addFacade( b.blockQuartzPillar.stack( 1 ) );

		try
		{
			this.initBuilderSupport();
		}
		catch (Throwable builderSupport)
		{
			// not supported?
		}

		Block skyStone = b.blockSkyStone.block();
		if ( skyStone != null )
		{
			this.addFacade( new ItemStack( skyStone, 1, 0 ) );
			this.addFacade( new ItemStack( skyStone, 1, 1 ) );
			this.addFacade( new ItemStack( skyStone, 1, 2 ) );
			this.addFacade( new ItemStack( skyStone, 1, 3 ) );
		}
	}

	private void initBuilderSupport()
	{
		final ISchematicRegistry schematicRegistry = BuilderAPI.schematicRegistry;

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
						schematicRegistry.registerSchematicBlock( myBlock, AERotatableBlockSchematic.class );
					}
					else if ( myBlock == cable )
					{
						schematicRegistry.registerSchematicBlock( myBlock, AECableSchematicTile.class );
					}
					else if ( def.entity() != null )
					{
						schematicRegistry.registerSchematicBlock( myBlock, AEGenericSchematicTile.class );
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
	public void postInit()
	{
		this.registerPowerP2P();
		this.registerItemP2P();
		this.registerLiquidsP2P();
	}

	@Override
	public IFacadePart createFacadePart(Block blk, int meta, ForgeDirection side)
	{
		try
		{
			final ItemFacade.FacadeState state = ItemFacade.FacadeState.create( blk, meta );
			final ItemStack facade = ItemFacade.getFacade( state );

			return new FacadePart( facade, side );
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
		final Item maybeFacadeItem = facade.getItem();

		if ( maybeFacadeItem instanceof buildcraft.api.facades.IFacadeItem)
		{
			final buildcraft.api.facades.IFacadeItem facadeItem = (buildcraft.api.facades.IFacadeItem) maybeFacadeItem;

			final Block[] blocks = facadeItem.getBlocksForFacade( facade );
			final int[] metas = facadeItem.getMetaValuesForFacade( facade );

			if ( blocks.length > 0 && metas.length > 0 )
			{
				return new ItemStack( blocks[0], 1, metas[0] );
			}
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
