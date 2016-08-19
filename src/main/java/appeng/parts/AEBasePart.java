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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.client.BakingPipeline;
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
import appeng.client.render.model.ModelsCache;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.networking.PartCable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public abstract class AEBasePart implements IPart, IGridProxyable, IActionHost, IUpgradeableHost, ICustomNameObject
{

	private static final Pattern PROPERTY_PATTERN = Pattern.compile( "\\$\\{([\\p{Alnum}_\\-\\.]+)\\}" );

	public static final ResourceLocation replaceProperties( ResourceLocation location, ImmutableMap<String, String> properties )
	{
		Matcher m = PROPERTY_PATTERN.matcher( location.getResourcePath() );
		StringBuffer buffer = new StringBuffer();
		while( m.find() )
		{
			m.appendReplacement( buffer, properties.get( m.group( 1 ) ) );
		}
		m.appendTail( buffer );
		return new ResourceLocation( location.getResourceDomain(), buffer.toString() );
	}

	protected static final Function<ResourceLocation, TextureAtlasSprite> propertyTextureGetter( ImmutableMap<String, String> properties )
	{
		return location -> ModelsCache.DEFAULTTEXTUREGETTER.apply( replaceProperties( location, properties ) );
	}

	protected static final Function<ResourceLocation, TextureAtlasSprite> propertyTextureGetter( ImmutableMap.Builder<String, String> properties )
	{
		return propertyTextureGetter( properties.build() );
	}

	protected static final ResourceLocation withProperties( ResourceLocation location, ImmutableMap.Builder<String, String> properties )
	{
		return new ResourceLocation( location.getResourceDomain(), location.getResourcePath() + properties.build().toString() );
	}

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
		return ItemMultiPart.instance.getTypeByStack( is );
	}

	public ResourceLocation getDefaultModelLocation()
	{
		return getType().getModel();
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
		if( this.getItemStack().stackSize > 0 )
		{
			final List<ItemStack> items = new ArrayList<ItemStack>();
			items.add( this.is.copy() );
			this.host.removePart( this.side, false );
			Platform.spawnDrops( this.tile.getWorld(), this.tile.getPos(), items );
			this.is.stackSize = 0;
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
	public boolean hasCustomName()
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
	public int getCableConnectionLength()
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

	@Override
	@SideOnly( Side.CLIENT )
	public List<BakedQuad> getOrBakeQuads( BakingPipeline<BakedQuad, BakedQuad> rotatingPipeline, IBlockState state, EnumFacing side, long rand )
	{
		return rotatingPipeline.pipe( ModelsCache.INSTANCE.getOrLoadModel( withProperties( getDefaultModelLocation(), propertiesForModel( getSide().getFacing() ) ), getDefaultModelLocation(), propertyTextureGetter( propertiesForModel( getSide().getFacing() ) ) ).getQuads( state, side, rand ), null, state, getSide().getFacing(), rand );
	}

	protected ImmutableMap.Builder<String, String> propertiesForModel( EnumFacing facing )
	{
		return ImmutableMap.<String, String>builder().put( "color", getColor().name() );
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