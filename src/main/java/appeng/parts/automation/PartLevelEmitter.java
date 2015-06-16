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

package appeng.parts.automation;


import java.util.Collection;
import java.util.Random;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.IAESprite;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public class PartLevelEmitter extends PartUpgradeable implements IEnergyWatcherHost, IStackWatcherHost, ICraftingWatcherHost, IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingProvider
{

	private static final int FLAG_ON = 4;

	final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 1 );

	boolean prevState = false;

	long lastReportedValue = 0;
	long reportingValue = 0;

	IStackWatcher myWatcher;
	IEnergyWatcher myEnergyWatcher;
	ICraftingWatcher myCraftingWatcher;
	double centerX;
	double centerY;
	double centerZ;
	boolean status = false;

	@Reflected
	public PartLevelEmitter( ItemStack is )
	{
		super( is );

		this.getConfigManager().registerSetting( Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL );
		this.getConfigManager().registerSetting( Settings.CRAFT_VIA_REDSTONE, YesNo.NO );
	}

	public long getReportingValue()
	{
		return this.reportingValue;
	}

	public void setReportingValue( long v )
	{
		this.reportingValue = v;
		if( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			this.configureWatchers();
		}
		else
		{
			this.updateState();
		}
	}

	@MENetworkEventSubscribe
	public void powerChanged( MENetworkPowerStatusChange c )
	{
		this.updateState();
	}

	private void updateState()
	{
		boolean isOn = this.isLevelEmitterOn();
		if( this.prevState != isOn )
		{
			this.host.markForUpdate();
			TileEntity te = this.host.getTile();
			this.prevState = isOn;
			Platform.notifyBlocksOfNeighbors( te.getWorld(), te.getPos() );
			Platform.notifyBlocksOfNeighbors( te.getWorld(), te.getPos().offset( side.getFacing() ) );
		}
	}

	private boolean isLevelEmitterOn()
	{
		if( Platform.isClient() )
		{
			return ( this.clientFlags & this.FLAG_ON ) == this.FLAG_ON;
		}

		if( !this.proxy.isActive() )
		{
			return false;
		}

		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			try
			{
				return this.proxy.getCrafting().isRequesting( this.config.getAEStackInSlot( 0 ) );
			}
			catch( GridAccessException e )
			{
				// :P
			}

			return this.prevState;
		}

		boolean flipState = this.getConfigManager().getSetting( Settings.REDSTONE_EMITTER ) == RedstoneMode.LOW_SIGNAL;
		return flipState ? this.reportingValue >= this.lastReportedValue + 1 : this.reportingValue < this.lastReportedValue + 1;
	}

	@MENetworkEventSubscribe
	public void channelChanged( MENetworkChannelsChanged c )
	{
		this.updateState();
	}

	@Override
	protected int populateFlags( int cf )
	{
		return cf | ( this.prevState ? this.FLAG_ON : 0 );
	}

	@Override
	public TextureAtlasSprite getBreakingTexture( IRenderHelper renderer )
	{
		return renderer.getIcon( is ).getAtlas();
	}

	@Override
	public void updateWatcher( ICraftingWatcher newWatcher )
	{
		this.myCraftingWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void onRequestChange( ICraftingGrid craftingGrid, IAEItemStack what )
	{
		this.updateState();
	}

	// update the system...
	public void configureWatchers()
	{
		IAEItemStack myStack = this.config.getAEStackInSlot( 0 );

		if( this.myWatcher != null )
		{
			this.myWatcher.clear();
		}

		if( this.myEnergyWatcher != null )
		{
			this.myEnergyWatcher.clear();
		}

		if( this.myCraftingWatcher != null )
		{
			this.myCraftingWatcher.clear();
		}

		try
		{
			this.proxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.proxy.getNode() ) );
		}
		catch( GridAccessException e1 )
		{
			// :/
		}

		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if( this.myCraftingWatcher != null && myStack != null )
			{
				this.myCraftingWatcher.add( myStack );
			}

			return;
		}

		if( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			if( this.myEnergyWatcher != null )
			{
				this.myEnergyWatcher.add( (double) this.reportingValue );
			}

			try
			{
				// update to power...
				this.lastReportedValue = (long) this.proxy.getEnergy().getStoredPower();
				this.updateState();

				// no more item stuff..
				this.proxy.getStorage().getItemInventory().removeListener( this );
			}
			catch( GridAccessException e )
			{
				// :P
			}

			return;
		}

		try
		{
			if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 || myStack == null )
			{
				this.proxy.getStorage().getItemInventory().addListener( this, this.proxy.getGrid() );
			}
			else
			{
				this.proxy.getStorage().getItemInventory().removeListener( this );

				if( this.myWatcher != null )
				{
					this.myWatcher.add( myStack );
				}
			}

			this.updateReportingValue( this.proxy.getStorage().getItemInventory() );
		}
		catch( GridAccessException e )
		{
			// >.>
		}
	}

	private void updateReportingValue( IMEMonitor<IAEItemStack> monitor )
	{
		IAEItemStack myStack = this.config.getAEStackInSlot( 0 );

		if( myStack == null )
		{
			this.lastReportedValue = 0;
			for( IAEItemStack st : monitor.getStorageList() )
			{
				this.lastReportedValue += st.getStackSize();
			}
		}
		else if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
		{
			this.lastReportedValue = 0;
			FuzzyMode fzMode = (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE );
			Collection<IAEItemStack> fuzzyList = monitor.getStorageList().findFuzzy( myStack, fzMode );
			for( IAEItemStack st : fuzzyList )
			{
				this.lastReportedValue += st.getStackSize();
			}
		}
		else
		{
			IAEItemStack r = monitor.getStorageList().findPrecise( myStack );
			if( r == null )
			{
				this.lastReportedValue = 0;
			}
			else
			{
				this.lastReportedValue = r.getStackSize();
			}
		}

		this.updateState();
	}

	@Override
	public void updateWatcher( IStackWatcher newWatcher )
	{
		this.myWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void onStackChange( IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan )
	{
		if( chan == StorageChannel.ITEMS && fullStack.equals( this.config.getAEStackInSlot( 0 ) ) && this.getInstalledUpgrades( Upgrades.FUZZY ) == 0 )
		{
			this.lastReportedValue = fullStack.getStackSize();
			this.updateState();
		}
	}

	@Override
	public void updateWatcher( IEnergyWatcher newWatcher )
	{
		this.myEnergyWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void onThresholdPass( IEnergyGrid energyGrid )
	{
		this.lastReportedValue = (long) energyGrid.getStoredPower();
		this.updateState();
	}

	@Override
	public boolean isValid( Object effectiveGrid )
	{
		try
		{
			return this.proxy.getGrid() == effectiveGrid;
		}
		catch( GridAccessException e )
		{
			return false;
		}
	}

	@Override
	public void postChange( IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, BaseActionSource actionSource )
	{
		this.updateReportingValue( (IMEMonitor<IAEItemStack>) monitor );
	}

	@Override
	public void onListUpdate()
	{
		try
		{
			this.updateReportingValue( this.proxy.getStorage().getItemInventory() );
		}
		catch( GridAccessException e )
		{
			// ;P
		}
	}

	@Override
	public AECableType getCableConnectionType( AEPartLocation dir )
	{
		return AECableType.SMART;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 7, 7, 11, 9, 9, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, IRenderHelper renderer )
	{
		rh.setTexture( renderer.getIcon( is ) );		
		this.renderTorchAtAngle( 0, -0.5, 0, renderer );
	}

	public void renderTorchAtAngle( double baseX, double baseY, double baseZ, IRenderHelper renderer )
	{
		boolean isOn = this.isLevelEmitterOn();
		IAESprite offTexture = renderer.getIcon( is );
		IAESprite IIcon = ( isOn ? CableBusTextures.LevelEmitterTorchOn.getIcon() : offTexture );
		//
		this.centerX = baseX + 0.5;
		this.centerY = baseY + 0.5;
		this.centerZ = baseZ + 0.5;

		baseY += 7.0 / 16.0;

		double par10 = 0;
		// double par11 = 0;
		double Zero = 0;

		/*
		 * double d5 = (double)TextureAtlasSprite.func_94209_e(); double d6 = (double)TextureAtlasSprite.func_94206_g(); double d7 =
		 * (double)TextureAtlasSprite.func_94212_f(); double d8 = (double)TextureAtlasSprite.func_94210_h(); double d9 =
		 * (double)TextureAtlasSprite.func_94214_a(7.0D); double d10 = (double)TextureAtlasSprite.func_94207_b(6.0D); double d11 =
		 * (double)TextureAtlasSprite.func_94214_a(9.0D); double d12 = (double)TextureAtlasSprite.func_94207_b(8.0D); double d13 =
		 * (double)TextureAtlasSprite.func_94214_a(7.0D); double d14 = (double)TextureAtlasSprite.func_94207_b(13.0D); double d15 =
		 * (double)TextureAtlasSprite.func_94214_a(9.0D); double d16 = (double)TextureAtlasSprite.func_94207_b(15.0D);
		 */

		float var16 = IIcon.getMinU();
		float var17 = IIcon.getMaxU();
		float var18 = IIcon.getMinV();
		float var19 = IIcon.getMaxV();
		/*
		 * float var16 = (float)var14 / 256.0F; float var17 = ((float)var14 + 15.99F) / 256.0F; float var18 =
		 * (float)var15 / 256.0F; float var19 = ((float)var15 + 15.99F) / 256.0F;
		 */
		double var20b = offTexture.getInterpolatedU( 7.0D );
		double var24b = offTexture.getInterpolatedU( 9.0D );

		double var20 = IIcon.getInterpolatedU( 7.0D );
		double var24 = IIcon.getInterpolatedU( 9.0D );
		double var22 = IIcon.getInterpolatedV( 6.0D + ( isOn ? 0 : 1.0D ) );
		double var26 = IIcon.getInterpolatedV( 8.0D + ( isOn ? 0 : 1.0D ) );
		double var28 = IIcon.getInterpolatedU( 7.0D );
		double var30 = IIcon.getInterpolatedV( 13.0D );
		double var32 = IIcon.getInterpolatedU( 9.0D );
		double var34 = IIcon.getInterpolatedV( 15.0D );

		double var22b = IIcon.getInterpolatedV( 9.0D );
		double var26b = IIcon.getInterpolatedV( 11.0D );

		baseX += 0.5D;
		baseZ += 0.5D;
		double var36 = baseX - 0.5D;
		double var38 = baseX + 0.5D;
		double var40 = baseZ - 0.5D;
		double var42 = baseZ + 0.5D;
		double var44 = 0.0625D;
		double var422 = 0.1915D + 1.0 / 16.0;
		double TorchLen = 0.625D;

		double toff = 0.0d;

		if( !isOn )
		{
			toff = 1.0d / 16.0d;
		}

		if( isOn )
		{
			renderer.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
			renderer.setBrightness( 11 << 20 | 11 << 4 );
		}

		EnumFacing t = EnumFacing.UP;
		
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var20, var22 );
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var20, var26 );
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var24, var26 );
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var24, var22 );

		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var24b, var22b );
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var24b, var26b );
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var20b, var26b );
		this.addVertexWithUV(t,renderer, baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var20b, var22b );

		this.addVertexWithUV(t,renderer, baseX + var44 + Zero, baseY, baseZ - var44 + par10, var32, var30 );
		this.addVertexWithUV(t,renderer, baseX + var44 + Zero, baseY, baseZ + var44 + par10, var32, var34 );
		this.addVertexWithUV(t,renderer, baseX - var44 + Zero, baseY, baseZ + var44 + par10, var28, var34 );
		this.addVertexWithUV(t,renderer, baseX - var44 + Zero, baseY, baseZ - var44 + par10, var28, var30 );

		this.addVertexWithUV(t,renderer, baseX - var44, baseY + 1.0D, var40, var16, var18 );
		this.addVertexWithUV(t,renderer, baseX - var44 + Zero, baseY + 0.0D, var40 + par10, var16, var19 );
		this.addVertexWithUV(t,renderer, baseX - var44 + Zero, baseY + 0.0D, var42 + par10, var17, var19 );
		this.addVertexWithUV(t,renderer, baseX - var44, baseY + 1.0D, var42, var17, var18 );

		this.addVertexWithUV(t,renderer, baseX + var44, baseY + 1.0D, var42, var16, var18 );
		this.addVertexWithUV(t,renderer, baseX + Zero + var44, baseY + 0.0D, var42 + par10, var16, var19 );
		this.addVertexWithUV(t,renderer, baseX + Zero + var44, baseY + 0.0D, var40 + par10, var17, var19 );
		this.addVertexWithUV(t,renderer, baseX + var44, baseY + 1.0D, var40, var17, var18 );

		this.addVertexWithUV(t,renderer, var36, baseY + 1.0D, baseZ + var44, var16, var18 );
		this.addVertexWithUV(t,renderer, var36 + Zero, baseY + 0.0D, baseZ + var44 + par10, var16, var19 );
		this.addVertexWithUV(t,renderer, var38 + Zero, baseY + 0.0D, baseZ + var44 + par10, var17, var19 );
		this.addVertexWithUV(t,renderer, var38, baseY + 1.0D, baseZ + var44, var17, var18 );

		this.addVertexWithUV(t,renderer, var38, baseY + 1.0D, baseZ - var44, var16, var18 );
		this.addVertexWithUV(t,renderer, var38 + Zero, baseY + 0.0D, baseZ - var44 + par10, var16, var19 );
		this.addVertexWithUV(t,renderer, var36 + Zero, baseY + 0.0D, baseZ - var44 + par10, var17, var19 );
		this.addVertexWithUV(t,renderer, var36, baseY + 1.0D, baseZ - var44, var17, var18 );
	}

	public void addVertexWithUV( EnumFacing face, IRenderHelper renderer, double x, double y, double z, double u, double v )
	{
		x -= this.centerX;
		y -= this.centerY;
		z -= this.centerZ;

		if( this.side == AEPartLocation.DOWN )
		{
			y = -y;
			z = -z;
		}

		if( this.side == AEPartLocation.EAST )
		{
			double m = x;
			x = y;
			y = m;
			y = -y;
		}

		if( this.side == AEPartLocation.WEST )
		{
			double m = x;
			x = -y;
			y = m;
		}

		if( this.side == AEPartLocation.SOUTH )
		{
			double m = z;
			z = y;
			y = m;
			y = -y;
		}

		if( this.side == AEPartLocation.NORTH )
		{
			double m = z;
			z = -y;
			y = m;
		}

		x += this.centerX;// + orientation.offsetX * 0.4;
		y += this.centerY;// + orientation.offsetY * 0.4;
		z += this.centerZ;// + orientation.offsetZ * 0.4;

		renderer.addVertexWithUV( face, x, y, z, u, v );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( BlockPos pos, IPartRenderHelper rh, IRenderHelper renderer )
	{
		rh.setTexture( renderer.getIcon( is ) );
		// rh.setTexture( CableBusTextures.ItemPartLevelEmitterOn.getIcon() );

		// rh.setBounds( 2, 2, 14, 14, 14, 16 );
		// rh.renderBlock( x, y, z, renderer );

		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderBlock( x, y, z, renderer );

		renderer.renderAllFaces = true;

		renderer.setBrightness( rh.getBlock().getMixedBrightnessForBlock( this.getHost().getTile().getWorld(), pos ) );
		renderer.setColorOpaque_F( 1.0F, 1.0F, 1.0F );

		this.renderTorchAtAngle( pos.getX(),pos.getY(),pos.getZ(),renderer );

		renderer.renderAllFaces = false;

		rh.setBounds( 7, 7, 11, 9, 9, 12 );
		this.renderLights( pos, rh, renderer );

		// super.renderWorldBlock( world, x, y, z, block, modelId, renderer );
	}

	@Override
	public int isProvidingStrongPower()
	{
		return this.prevState ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return this.prevState ? 15 : 0;
	}

	@Override
	public void randomDisplayTick(
			World world,
			BlockPos pos,
			Random r )
	{
		if( this.isLevelEmitterOn() )
		{
			AEPartLocation d = this.side;

			double d0 = d.xOffset * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;
			double d1 = d.yOffset * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;
			double d2 = d.zOffset * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;

			world.spawnParticle( EnumParticleTypes.REDSTONE, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1, 0.5 + pos.getZ() + d2, 0.0D, 0.0D, 0.0D, new int[0] );
		}
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 16;
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		if( !player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( player, this.getHost().getTile(), this.side, GuiBridge.GUI_LEVEL_EMITTER );
			return true;
		}

		return false;
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		this.configureWatchers();
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		if( inv == this.config )
		{
			this.configureWatchers();
		}

		super.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public void upgradesChanged()
	{
		this.configureWatchers();
	}

	@Override
	public boolean canConnectRedstone()
	{
		return true;
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.lastReportedValue = data.getLong( "lastReportedValue" );
		this.reportingValue = data.getLong( "reportingValue" );
		this.prevState = data.getBoolean( "prevState" );
		this.config.readFromNBT( data, "config" );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setLong( "lastReportedValue", this.lastReportedValue );
		data.setLong( "reportingValue", this.reportingValue );
		data.setBoolean( "prevState", this.prevState );
		this.config.writeToNBT( data, "config" );
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		if( name.equals( "config" ) )
		{
			return this.config;
		}

		return super.getInventoryByName( name );
	}

	@Override
	public boolean pushPattern( ICraftingPatternDetails patternDetails, InventoryCrafting table )
	{
		return false;
	}

	@Override
	public boolean isBusy()
	{
		return true;
	}

	@Override
	public void provideCrafting( ICraftingProviderHelper craftingTracker )
	{
		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if( this.getConfigManager().getSetting( Settings.CRAFT_VIA_REDSTONE ) == YesNo.YES )
			{
				IAEItemStack what = this.config.getAEStackInSlot( 0 );
				if( what != null )
				{
					craftingTracker.setEmitable( what );
				}
			}
		}
	}
}
