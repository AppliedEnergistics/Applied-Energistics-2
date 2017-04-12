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

package appeng.integration.modules;


import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.parts.IFacadePart;
import appeng.facade.FacadePart;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.integration.modules.BCHelpers.BCPipeHandler;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * @author thatsIch
 * @version rv3 - 12.06.2015
 * @since rv3 12.06.2015
 */
@Reflected
public class BuildCraftTransport implements IBuildCraftTransport, IIntegrationModule
{
	@Reflected
	public static BuildCraftTransport instance;

	@Reflected
	public BuildCraftTransport()
	{
		IntegrationHelper.testClassExistence( this, buildcraft.BuildCraftTransport.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.facades.IFacadeItem.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.transport.IInjectable.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.transport.IPipeConnection.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.transport.IPipeTile.class );
		IntegrationHelper.testClassExistence( this, buildcraft.api.transport.IPipeTile.PipeType.class );
		IntegrationHelper.testClassExistence( this, buildcraft.transport.ItemFacade.class );
		IntegrationHelper.testClassExistence( this, buildcraft.transport.PipeIconProvider.class );
	}

	@Override
	public boolean isFacade( final ItemStack is )
	{
		if( is == null )
		{
			return false;
		}

		return is.getItem() instanceof IFacadeItem;
	}

	@Nullable
	@Override
	public IFacadePart createFacadePart( final Block blk, final int meta, @Nonnull final ForgeDirection side )
	{
		try
		{
			final ItemFacade.FacadeState state = ItemFacade.FacadeState.create( blk, meta );
			final ItemStack facade = ItemFacade.getFacade( state );

			return new FacadePart( facade, side );
		}
		catch( final Exception ignored )
		{

		}

		return null;
	}

	@Override
	public IFacadePart createFacadePart( @Nonnull final ItemStack fs, @Nonnull final ForgeDirection side )
	{
		return new FacadePart( fs, side );
	}

	@Nullable
	@Override
	public ItemStack getTextureForFacade( @Nonnull final ItemStack facade )
	{
		final Item maybeFacadeItem = facade.getItem();

		if( maybeFacadeItem instanceof IFacadeItem )
		{
			final IFacadeItem facadeItem = (IFacadeItem) maybeFacadeItem;

			final Block[] blocks = facadeItem.getBlocksForFacade( facade );
			final int[] metas = facadeItem.getMetaValuesForFacade( facade );

			if( blocks.length > 0 && metas.length > 0 )
			{
				return new ItemStack( blocks[0], 1, metas[0] );
			}
		}

		return null;
	}

	@Nullable
	@Override
	public IIcon getCobbleStructurePipeTexture()
	{
		try
		{
			return buildcraft.BuildCraftTransport.instance.pipeIconProvider.getIcon( PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal() ); // Structure
		}
		catch( final Exception ignored )
		{
		}
		return null;
		// Pipe
	}

	@Override
	public boolean isPipe( final TileEntity te, @Nonnull final ForgeDirection dir )
	{
		if( te instanceof IPipeTile )
		{
			final IPipeTile pipeTile = (IPipeTile) te;
			return !pipeTile.hasPipePluggable( dir.getOpposite() );
		}

		return false;
	}

	@Override
	public boolean canAddItemsToPipe( final TileEntity te, final ItemStack is, final ForgeDirection dir )
	{
		if( is != null && te != null && te instanceof IInjectable )
		{
			final IInjectable pt = (IInjectable) te;
			if( pt.canInjectItems( dir ) )
			{
				final int amt = pt.injectItem( is, false, dir, null );
				if( amt == is.stackSize )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean addItemsToPipe( @Nullable final TileEntity te, @Nullable final ItemStack is, @Nonnull final ForgeDirection dir )
	{
		if( is != null && te != null && te instanceof IInjectable )
		{
			final IInjectable pt = (IInjectable) te;
			if( pt.canInjectItems( dir ) )
			{
				final int amt = pt.injectItem( is, false, dir, null );
				if( amt == is.stackSize )
				{
					pt.injectItem( is, true, dir, null );
					return true;
				}
			}
		}

		return false;
	}

	private void addFacade( final ItemStack item )
	{
		if( item != null )
		{
			FMLInterModComms.sendMessage( "BuildCraft|Transport", "add-facade", item );
		}
	}

	private void registerPowerP2P()
	{
		final IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();

		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipePowerCobblestone ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipePowerDiamond ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipePowerGold ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipePowerQuartz ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipePowerStone ), TunnelType.RF_POWER );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipePowerWood ), TunnelType.RF_POWER );
	}

	private void registerItemP2P()
	{
		final IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();

		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsWood ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsVoid ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsSandstone ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsQuartz ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsObsidian ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsIron ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsGold ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsEmerald ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsDiamond ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsStone ), TunnelType.ITEM );
		registry.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeItemsCobblestone ), TunnelType.ITEM );
	}

	private void registerLiquidsP2P()
	{
		final IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsCobblestone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsEmerald ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsGold ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsIron ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsSandstone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsStone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsVoid ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( buildcraft.BuildCraftTransport.pipeFluidsWood ), TunnelType.FLUID );
	}

	@Override
	public void init() throws Throwable
	{
		this.initPipeConnection();
		this.initFacades();
	}

	@Override
	public void postInit()
	{
		this.registerPowerP2P();
		this.registerItemP2P();
		this.registerLiquidsP2P();
	}

	private void initPipeConnection()
	{
		final IAppEngApi api = AEApi.instance();

		api.partHelper().registerNewLayer( "appeng.parts.layers.LayerIPipeConnection", "buildcraft.api.transport.IPipeConnection" );
		api.registries().externalStorage().addExternalStorageInterface( new BCPipeHandler() );
	}

	private void initFacades()
	{
		final IAppEngApi api = AEApi.instance();
		final IBlocks blocks = api.definitions().blocks();

		this.addFacadeStack( blocks.fluix() );
		this.addFacadeStack( blocks.quartz() );
		this.addFacadeStack( blocks.quartzChiseled() );
		this.addFacadeStack( blocks.quartzPillar() );

		for( final Block skyStoneBlock : blocks.skyStone().maybeBlock().asSet() )
		{
			this.addFacade( new ItemStack( skyStoneBlock, 1, 0 ) );
			this.addFacade( new ItemStack( skyStoneBlock, 1, 1 ) );
			this.addFacade( new ItemStack( skyStoneBlock, 1, 2 ) );
			this.addFacade( new ItemStack( skyStoneBlock, 1, 3 ) );
		}
	}

	private void addFacadeStack( final IItemDefinition definition )
	{
		for( final ItemStack facadeStack : definition.maybeStack( 1 ).asSet() )
		{
			this.addFacade( facadeStack );
		}
	}
}
