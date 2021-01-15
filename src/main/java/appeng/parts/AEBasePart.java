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


import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.parts.PartFluidLevelEmitter;
import appeng.fluids.util.AEFluidInventory;
import appeng.parts.automation.PartLevelEmitter;
import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IDefinitions;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.networking.PartCable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public abstract class AEBasePart implements IPart, IGridProxyable, IActionHost, IUpgradeableHost, ICustomNameObject
{

	private final AENetworkProxy proxy;
	private final ItemStack is;
	private TileEntity tile = null;
	private IPartHost host = null;
	private AEPartLocation side = null;

	public AEBasePart( final ItemStack is )
	{
		Preconditions.checkNotNull( is );

		this.is = is;
		this.proxy = new AENetworkProxy( this, "part", is, this instanceof PartCable );
		this.proxy.setValidSides( EnumSet.noneOf( EnumFacing.class ) );
	}

	public IPartHost getHost()
	{
		return this.host;
	}

	public PartType getType()
	{
		return ItemPart.instance.getTypeByStack( this.is );
	}

	@Override
	public IGridNode getGridNode( final AEPartLocation dir )
	{
		return this.proxy.getNode();
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void securityBreak()
	{
		if( this.getItemStack().getCount() > 0 && this.getGridNode() != null )
		{
			final List<ItemStack> items = new ArrayList<>();
			items.add( this.is.copy() );
			this.host.removePart( this.side, false );
			Platform.spawnDrops( this.tile.getWorld(), this.tile.getPos(), items );
			this.is.setCount( 0 );
		}
	}

	protected AEColor getColor()
	{
		if( this.host == null )
		{
			return AEColor.TRANSPARENT;
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
	public String getCustomInventoryName()
	{
		return this.getItemStack().getDisplayName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.getItemStack().hasDisplayName();
	}

	public void addEntityCrashInfo( final CrashReportCategory crashreportcategory )
	{
		crashreportcategory.addCrashSection( "Part Side", this.getSide() );
	}

	@Override
	public ItemStack getItemStack( final PartItemStack type )
	{
		if( type == PartItemStack.NETWORK )
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
	public void onNeighborChanged( IBlockAccess w, BlockPos pos, BlockPos neighbor )
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
	public void setPartHostInfo( final AEPartLocation side, final IPartHost host, final TileEntity tile )
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
	public void randomDisplayTick( final World world, final BlockPos pos, final Random r )
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
	public float getCableConnectionLength( AECableType cable )
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
	public IItemHandler getInventoryByName( final String name )
	{
		return null;
	}

	/**
	 * depending on the from, different settings will be accepted, don't call this with null
	 *
	 * @param from source of settings
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

		final IItemHandler inv = this.getInventoryByName( "config" );
		if( inv instanceof AppEngInternalAEInventory )
		{
			final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
			final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSlots() );
			tmp.readFromNBT( compound, "config" );
			for( int x = 0; x < tmp.getSlots(); x++ )
			{
				target.setStackInSlot( x, tmp.getStackInSlot( x ) );
			}
			if (this instanceof PartLevelEmitter ) {
				final PartLevelEmitter partLevelEmitter = (PartLevelEmitter) this;
				partLevelEmitter.setReportingValue(compound.getLong("reportingValue"));
			}
		}

		if (this instanceof IConfigurableFluidInventory) {
			final IFluidHandler tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
			if (tank instanceof AEFluidInventory ) {
				final AEFluidInventory target = (AEFluidInventory) tank;
				final AEFluidInventory tmp = new AEFluidInventory(null, target.getSlots());
				tmp.readFromNBT(compound, "config");
				for (int x = 0; x < tmp.getSlots(); x++) {
					target.setFluidInSlot(x, tmp.getFluidInSlot(x));
				}
			}
			if (this instanceof PartFluidLevelEmitter ) {
				final PartFluidLevelEmitter partFluidLevelEmitter = (PartFluidLevelEmitter) this;
				partFluidLevelEmitter.setReportingValue(compound.getLong("reportingValue"));
			}
		}
	}

	/**
	 * null means nothing to store...
	 *
	 * @param from source of settings
	 *
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

		final IItemHandler inv = this.getInventoryByName( "config" );
		if( inv instanceof AppEngInternalAEInventory )
		{
			( (AppEngInternalAEInventory) inv ).writeToNBT( output, "config" );
			if (this instanceof PartLevelEmitter) {
				final PartLevelEmitter partLevelEmitter = (PartLevelEmitter) this;
				output.setLong("reportingValue", partLevelEmitter.getReportingValue());
			}
		}

		if (this instanceof IConfigurableFluidInventory) {
			final IFluidHandler tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
			if (tank instanceof AEFluidInventory ) {
				((AEFluidInventory) tank).writeToNBT(output, "config");
			}
			if (this instanceof PartFluidLevelEmitter) {
				final PartFluidLevelEmitter partFluidLevelEmitter = (PartFluidLevelEmitter) this;
				output.setLong("reportingValue", partFluidLevelEmitter.getReportingValue());
			}
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

		if( !memCardIS.isEmpty() && this.useStandardMemoryCard() && memCardIS.getItem() instanceof IMemoryCard )
		{
			final IMemoryCard memoryCard = (IMemoryCard) memCardIS.getItem();

			ItemStack is = this.getItemStack( PartItemStack.NETWORK );

			// Blocks and parts share the same soul!
			final IDefinitions definitions = AEApi.instance().definitions();
			if( definitions.parts().iface().isSameAs( is ) )
			{
				Optional<ItemStack> iface = definitions.blocks().iface().maybeStack( 1 );
				if( iface.isPresent() )
				{
					is = iface.get();
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
	public final boolean onActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( this.useMemoryCard( player ) )
		{
			return true;
		}

		return this.onPartActivate( player, hand, pos );
	}

	@Override
	public final boolean onShiftActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( this.useMemoryCard( player ) )
		{
			return true;
		}

		return this.onPartShiftActivate( player, hand, pos );
	}

	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		return false;
	}

	public boolean onPartShiftActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		return false;
	}

	@Override
	public void onPlacement( final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side )
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

	public AEPartLocation getSide()
	{
		return this.side;
	}

	private void setSide( final AEPartLocation side )
	{
		this.side = side;
	}

	public ItemStack getItemStack()
	{
		return this.is;
	}
}