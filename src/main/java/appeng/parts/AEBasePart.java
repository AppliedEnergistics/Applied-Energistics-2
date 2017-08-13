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

package appeng.parts;


import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IDefinitions;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.networking.PartCable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import com.google.common.base.Preconditions;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;


public abstract class AEBasePart implements IPart, IGridProxyable, IActionHost, IUpgradeableHost, ICustomNameObject
{

	private final AENetworkProxy proxy;
	private final ItemStack is;
	private ISimplifiedBundle renderCache = null;
	private TileEntity tile = null;
	private IPartHost host = null;
	private ForgeDirection side = null;

	public AEBasePart( final ItemStack is )
	{
		Preconditions.checkNotNull( is );

		this.is = is;
		this.proxy = new AENetworkProxy( this, "part", is, this instanceof PartCable );
		this.proxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	public IPartHost getHost()
	{
		return this.host;
	}

	@Override
	public IGridNode getGridNode( final ForgeDirection dir )
	{
		return this.proxy.getNode();
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void securityBreak()
	{
		if( this.getItemStack().stackSize > 0 )
		{
			final List<ItemStack> items = new ArrayList<ItemStack>();
			items.add( this.getItemStack().copy() );
			this.host.removePart( this.getSide(), false );
			Platform.spawnDrops( this.tile.getWorldObj(), this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, items );
			this.getItemStack().stackSize = 0;
		}
	}

	protected AEColor getColor()
	{
		if( this.host == null )
		{
			return AEColor.Transparent;
		}
		return this.host.getColor();
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{

	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return 0;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public TileEntity getTile()
	{
		return this.tile;
	}

	@Override
	public AENetworkProxy getProxy()
	{
		return this.proxy;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.tile );
	}

	@Override
	public void gridChanged()
	{

	}

	@Override
	public IGridNode getActionableNode()
	{
		return this.proxy.getNode();
	}

	public void saveChanges()
	{
		this.host.markForSave();
	}

	@Override
	public String getCustomName()
	{
		return this.getItemStack().getDisplayName();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderBlock( x, y, z, renderer );
	}

	@Override
	public boolean hasCustomName()
	{
		return this.getItemStack().hasDisplayName();
	}

	public void addEntityCrashInfo( final CrashReportCategory crashreportcategory )
	{
		crashreportcategory.addCrashSection( "Part Side", this.getSide() );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderDynamic( final double x, final double y, final double z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{

	}

	@Override
	public ItemStack getItemStack( final PartItemStack type )
	{
		if( type == PartItemStack.Network )
		{
			final ItemStack copy = this.is.copy();
			copy.setTagCompound( null );
			return copy;
		}
		return this.is;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		this.proxy.readFromNBT( data );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		this.proxy.writeToNBT( data );
	}

	@Override
	public int isProvidingStrongPower()
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return 0;
	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{

	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		return false;
	}

	@Override
	public IGridNode getGridNode()
	{
		return this.proxy.getNode();
	}

	@Override
	public void onEntityCollision( final Entity entity )
	{

	}

	@Override
	public void removeFromWorld()
	{
		this.proxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		this.proxy.onReady();
	}

	@Override
	public void setPartHostInfo( final ForgeDirection side, final IPartHost host, final TileEntity tile )
	{
		this.setSide( side );
		this.tile = tile;
		this.host = host;
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return null;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{

	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{

	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	@Override
	public boolean isLadder( final EntityLivingBase entity )
	{
		return false;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return null;
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		return null;
	}

	/**
	 * depending on the from, different settings will be accepted, don't call this with null
	 *
	 * @param from     source of settings
	 * @param compound compound of source
	 */
	private void uploadSettings( final SettingsFrom from, final NBTTagCompound compound )
	{
		if( compound != null )
		{
			final IConfigManager cm = this.getConfigManager();
			if( cm != null )
			{
				cm.readFromNBT( compound );
			}
		}

		if( this instanceof IPriorityHost )
		{
			final IPriorityHost pHost = (IPriorityHost) this;
			pHost.setPriority( compound.getInteger( "priority" ) );
		}

		final IInventory inv = this.getInventoryByName( "config" );
		if( inv instanceof AppEngInternalAEInventory )
		{
			final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
			final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSizeInventory() );
			tmp.readFromNBT( compound, "config" );
			for( int x = 0; x < tmp.getSizeInventory(); x++ )
			{
				target.setInventorySlotContents( x, tmp.getStackInSlot( x ) );
			}
		}
	}

	/**
	 * null means nothing to store...
	 *
	 * @param from source of settings
	 * @return compound of source
	 */
	private NBTTagCompound downloadSettings( final SettingsFrom from )
	{
		final NBTTagCompound output = new NBTTagCompound();

		final IConfigManager cm = this.getConfigManager();
		if( cm != null )
		{
			cm.writeToNBT( output );
		}

		if( this instanceof IPriorityHost )
		{
			final IPriorityHost pHost = (IPriorityHost) this;
			output.setInteger( "priority", pHost.getPriority() );
		}

		final IInventory inv = this.getInventoryByName( "config" );
		if( inv instanceof AppEngInternalAEInventory )
		{
			( (AppEngInternalAEInventory) inv ).writeToNBT( output, "config" );
		}

		return output.hasNoTags() ? null : output;
	}

	public boolean useStandardMemoryCard()
	{
		return true;
	}

	private boolean useMemoryCard( final EntityPlayer player )
	{
		final ItemStack memCardIS = player.inventory.getCurrentItem();
		if( ForgeEventFactory.onItemUseStart( player, memCardIS, 1 ) <= 0 )
			return false;

		if( memCardIS != null && this.useStandardMemoryCard() && memCardIS.getItem() instanceof IMemoryCard )
		{
			final IMemoryCard memoryCard = (IMemoryCard) memCardIS.getItem();

			ItemStack is = this.getItemStack( PartItemStack.Network );

			// Blocks and parts share the same soul!
			final IDefinitions definitions = AEApi.instance().definitions();
			if( definitions.parts().iface().isSameAs( is ) )
			{
				for( final ItemStack iface : definitions.blocks().iface().maybeStack( 1 ).asSet() )
				{
					is = iface;
				}
			}

			final String name = is.getUnlocalizedName();

			if( player.isSneaking() )
			{
				final NBTTagCompound data = this.downloadSettings( SettingsFrom.MEMORY_CARD );
				if( data != null )
				{
					memoryCard.setMemoryCardContents( memCardIS, name, data );
					memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
				}
			}
			else
			{
				final String storedName = memoryCard.getSettingsName( memCardIS );
				final NBTTagCompound data = memoryCard.getData( memCardIS );
				if( name.equals( storedName ) )
				{
					this.uploadSettings( SettingsFrom.MEMORY_CARD, data );
					memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
				}
				else
				{
					memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public final boolean onActivate( final EntityPlayer player, final Vec3 pos )
	{
		//		int x = (int) pos.xCoord, y = (int) pos.yCoord, z = (int) pos.zCoord;
		int x = this.tile.xCoord, y = this.tile.yCoord, z = this.tile.zCoord;
		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract( player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, this.side.flag, player.getEntityWorld() );
		if( event.isCanceled() )
			return false;

		if( this.useMemoryCard( player ) )
		{
			return true;
		}

		return onPartActivate( player, pos );
	}

	@Override
	public final boolean onShiftActivate( final EntityPlayer player, final Vec3 pos )
	{
		//		int x = (int) pos.xCoord, y = (int) pos.yCoord, z = (int) pos.zCoord;
		int x = this.tile.xCoord, y = this.tile.yCoord, z = this.tile.zCoord;
		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract( player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, this.side.flag, player.getEntityWorld() );
		if( event.isCanceled() )
			return false;

		if( this.useMemoryCard( player ) )
		{
			return true;
		}

		return this.onPartShiftActivate( player, pos );
	}

	public boolean onPartActivate( final EntityPlayer player, final Vec3 pos )
	{
		return false;
	}

	public boolean onPartShiftActivate( final EntityPlayer player, final Vec3 pos )
	{
		return false;
	}

	@Override
	public void onPlacement( final EntityPlayer player, final ItemStack held, final ForgeDirection side )
	{
		this.proxy.setOwner( player );
	}

	@Override
	public boolean canBePlacedOn( final BusSupport what )
	{
		return what == BusSupport.CABLE;
	}

	@Override
	public boolean requireDynamicRender()
	{
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getBreakingTexture()
	{
		return null;
	}

	public ForgeDirection getSide()
	{
		return this.side;
	}

	private void setSide( final ForgeDirection side )
	{
		this.side = side;
	}

	public ItemStack getItemStack()
	{
		return this.is;
	}

	public ISimplifiedBundle getRenderCache()
	{
		return this.renderCache;
	}

	public void setRenderCache( final ISimplifiedBundle renderCache )
	{
		this.renderCache = renderCache;
	}
}