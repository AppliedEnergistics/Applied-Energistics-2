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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelEmitterRateMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
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
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public class PartLevelEmitter extends PartUpgradeable implements IEnergyWatcherHost, IStackWatcherHost, ICraftingWatcherHost,
		IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingProvider, IGridTickable
{

	public static final int FLAG_ON = 4;

	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 1 );

	private boolean prevState = false;

	private long lastReportedValue = 0;
	private long reportingValue = 0;

	private long lastReportedDiffValue = 0;

	/**
	 * The internal redstone strength, can differ from the output
	 */
	private int strength = 0;

	/**
	 * Previous redstone strength to detect changes
	 */
	private int prevStrength = 0;

	/**
	 * The output strength, can differ from the internal one if the emitter is flipped.
	 */
	private int outputStrength = 0;

	private LevelEmitterRateMode rateMode = LevelEmitterRateMode.BALANCED;

	private IStackWatcher myWatcher;
	private IEnergyWatcher myEnergyWatcher;
	private ICraftingWatcher myCraftingWatcher;

	public long getReportingValue()
	{
		return this.reportingValue;
	}

	public void setReportingValue( long v )
	{
		this.reportingValue = v;
		if ( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			this.configureWatchers();
		}
		else
		{
			this.calculateLevelEmitterStrength( false );
			this.updateState();
		}
	}

	@MENetworkEventSubscribe
	public void powerChanged( MENetworkPowerStatusChange c )
	{
		this.updateState();
	}

	@MENetworkEventSubscribe
	public void channelChanged( MENetworkChannelsChanged c )
	{
		this.updateState();
	}

	@Override
	public void upgradesChanged()
	{
		this.configureWatchers();
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		this.configureWatchers();
	}

	private void updateState()
	{
		if ( this.proxy.isActive() )
		{
			final boolean isOn = this.isLevelEmitterOn();
			if ( this.prevState != isOn || this.prevStrength != this.outputStrength )
			{
				this.host.markForUpdate();
				final TileEntity te = this.host.getTile();
				this.prevState = isOn;
				this.prevStrength = this.outputStrength;
				Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord );
				Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord + this.side.offsetX, te.yCoord + this.side.offsetY, te.zCoord + this.side.offsetZ );
			}
		}
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
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.lastReportedValue = data.getLong( "lastReportedValue" );
		this.reportingValue = data.getLong( "reportingValue" );
		this.prevState = data.getBoolean( "prevState" );
		this.config.readFromNBT( data, "config" );
	}

	@Override
	protected int populateFlags( int cf )
	{
		return cf | ( this.prevState ? this.FLAG_ON : 0 );
	}

	@Override
	public void updateWatcher( ICraftingWatcher newWatcher )
	{
		this.myCraftingWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void updateWatcher( IStackWatcher newWatcher )
	{
		this.myWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void updateWatcher( IEnergyWatcher newWatcher )
	{
		this.myEnergyWatcher = newWatcher;
		this.configureWatchers();
	}

	// update the system...
	public void configureWatchers()
	{
		final IAEItemStack myStack = this.config.getAEStackInSlot( 0 );

		if ( this.myWatcher != null )
		{
			this.myWatcher.clear();
		}

		if ( this.myEnergyWatcher != null )
		{
			this.myEnergyWatcher.clear();
		}

		if ( this.myCraftingWatcher != null )
		{
			this.myCraftingWatcher.clear();
		}

		this.calculateLevelEmitterStrength( true );

		try
		{
			this.proxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.proxy.getNode() ) );
		}
		catch ( final GridAccessException e1 )
		{
			// :/
		}

		if ( this.getInstalledUpgrades( Upgrades.SPEED ) > 0 )
		{
			rateMode = ( LevelEmitterRateMode ) this.getConfigManager().getSetting( Settings.LEVEL_EMITTER_RATE_MODE );
			try
			{
				this.proxy.getTick().alertDevice( this.proxy.getNode() );
			}
			catch ( final GridAccessException e )
			{
				// :P
			}
		}

		if ( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if ( this.myCraftingWatcher != null && myStack != null )
			{
				this.myCraftingWatcher.add( myStack );
			}

			this.updateState();

			return;
		}

		if ( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			if ( this.myEnergyWatcher != null )
			{
				this.myEnergyWatcher.add( ( double ) this.reportingValue );
			}

			try
			{
				// update to power...
				this.lastReportedValue = ( long ) this.proxy.getEnergy().getStoredPower();
				this.updateState();

				// no more item stuff..
				this.proxy.getStorage().getItemInventory().removeListener( this );
			}
			catch ( final GridAccessException e )
			{
				// :P
			}

			return;
		}

		try
		{
			if ( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 || myStack == null )
			{
				this.proxy.getStorage().getItemInventory().addListener( this, this.proxy.getGrid() );
			}
			else
			{
				this.proxy.getStorage().getItemInventory().removeListener( this );

				if ( this.myWatcher != null )
				{
					this.myWatcher.add( myStack );
				}
			}

			this.updateReportingValue( this.proxy.getStorage().getItemInventory() );
		}
		catch ( final GridAccessException e )
		{
			// >.>
		}
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		if ( inv == this.config )
		{
			this.configureWatchers();
		}

		super.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public void postChange( IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, BaseActionSource actionSource )
	{
		this.updateReportingValue( ( IMEMonitor<IAEItemStack> ) monitor );
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( player, this.getHost().getTile(), this.side, GuiBridge.GUI_LEVEL_EMITTER );
			return true;
		}

		return false;
	}

	private void updateReportingValue( IMEMonitor<IAEItemStack> monitor )
	{
		final IAEItemStack myStack = this.config.getAEStackInSlot( 0 );

		if ( myStack == null )
		{
			this.lastReportedValue = 0;
			for ( final IAEItemStack st : monitor.getStorageList() )
			{
				this.lastReportedValue += st.getStackSize();
			}
		}
		else if ( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
		{
			this.lastReportedValue = 0;
			final FuzzyMode fzMode = ( FuzzyMode ) this.getConfigManager().getSetting( Settings.FUZZY_MODE );
			final Collection<IAEItemStack> fuzzyList = monitor.getStorageList().findFuzzy( myStack, fzMode );
			for ( final IAEItemStack st : fuzzyList )
			{
				this.lastReportedValue += st.getStackSize();
			}
		}
		else
		{
			final IAEItemStack r = monitor.getStorageList().findPrecise( myStack );
			if ( r == null )
			{
				this.lastReportedValue = 0;
			}
			else
			{
				this.lastReportedValue = r.getStackSize();
			}
		}

		this.calculateLevelEmitterStrength( false );
		this.updateState();
	}

	@Override
	public boolean isValid( Object effectiveGrid )
	{
		try
		{
			return this.proxy.getGrid() == effectiveGrid;
		}
		catch ( final GridAccessException e )
		{
			return false;
		}
	}

	@Override
	public void onRequestChange( ICraftingGrid craftingGrid, IAEItemStack what )
	{
		this.calculateLevelEmitterStrength( false );
		this.updateState();
	}

	@Override
	public void onThresholdPass( IEnergyGrid energyGrid )
	{
		this.lastReportedValue = ( long ) energyGrid.getStoredPower();
		this.calculateLevelEmitterStrength( false );
		this.updateState();
	}

	@Override
	public void onStackChange( IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan )
	{
		if ( chan == StorageChannel.ITEMS && fullStack.equals( this.config.getAEStackInSlot( 0 ) ) && this.getInstalledUpgrades( Upgrades.FUZZY ) == 0 )
		{
			this.lastReportedValue = fullStack.getStackSize();

			if ( this.rateMode == LevelEmitterRateMode.BALANCED )
			{
				this.lastReportedDiffValue += diffStack.getStackSize();
			}

			if ( this.rateMode == LevelEmitterRateMode.POSITIVE && diffStack.getStackSize() > 0 )
			{
				this.lastReportedDiffValue += diffStack.getStackSize();
			}

			if ( this.rateMode == LevelEmitterRateMode.NEGATIVE && diffStack.getStackSize() < 0 )
			{
				this.lastReportedDiffValue += diffStack.getStackSize();
			}

			if ( this.getInstalledUpgrades( Upgrades.SPEED ) == 0 )
			{
				this.calculateLevelEmitterStrength( false );
			}

			this.updateState();
		}
	}

	public PartLevelEmitter( ItemStack is )
	{
		super( PartLevelEmitter.class, is );
		this.getConfigManager().registerSetting( Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL );
		this.getConfigManager().registerSetting( Settings.CRAFT_VIA_REDSTONE, YesNo.NO );
		this.getConfigManager().registerSetting( Settings.LEVEL_EMITTER_RATE_MODE, LevelEmitterRateMode.BALANCED );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setTexture( this.is.getIconIndex() );
		Tessellator.instance.startDrawingQuads();
		this.renderTorchAtAngle( 0, -0.5, 0 );
		Tessellator.instance.draw();
		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderInventoryBox( renderer );
	}

	private double centerX;
	private double centerY;
	private double centerZ;

	public void addVertexWithUV( double x, double y, double z, double u, double v )
	{
		final Tessellator var12 = Tessellator.instance;

		x -= this.centerX;
		y -= this.centerY;
		z -= this.centerZ;

		if ( this.side == ForgeDirection.DOWN )
		{
			y = -y;
			z = -z;
		}

		if ( this.side == ForgeDirection.EAST )
		{
			final double m = x;
			x = y;
			y = m;
			y = -y;
		}

		if ( this.side == ForgeDirection.WEST )
		{
			final double m = x;
			x = -y;
			y = m;
		}

		if ( this.side == ForgeDirection.SOUTH )
		{
			final double m = z;
			z = y;
			y = m;
			y = -y;
		}

		if ( this.side == ForgeDirection.NORTH )
		{
			final double m = z;
			z = -y;
			y = m;
		}

		x += this.centerX;// + orientation.offsetX * 0.4;
		y += this.centerY;// + orientation.offsetY * 0.4;
		z += this.centerZ;// + orientation.offsetZ * 0.4;

		var12.addVertexWithUV( x, y, z, u, v );
	}

	@Override
	public void randomDisplayTick( World world, int x, int y, int z, Random r )
	{
		if ( this.isLevelEmitterOn() )
		{
			final ForgeDirection d = this.side;

			final double d0 = d.offsetX * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;
			final double d1 = d.offsetY * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;
			final double d2 = d.offsetZ * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;

			world.spawnParticle( "reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );
		}
	}

	public void renderTorchAtAngle( double baseX, double baseY, double baseZ )
	{
		final boolean isOn = this.isLevelEmitterOn();
		final IIcon offTexture = this.is.getIconIndex();
		final IIcon IIcon = ( isOn ? CableBusTextures.LevelEmitterTorchOn.getIcon() : offTexture );
		//
		this.centerX = baseX + 0.5;
		this.centerY = baseY + 0.5;
		this.centerZ = baseZ + 0.5;

		baseY += 7.0 / 16.0;

		final double par10 = 0;
		// double par11 = 0;
		final double Zero = 0;

		/*
		 * double d5 = (double)IIcon.func_94209_e(); double d6 = (double)IIcon.func_94206_g(); double d7 =
		 * (double)IIcon.func_94212_f(); double d8 = (double)IIcon.func_94210_h(); double d9 =
		 * (double)IIcon.func_94214_a(7.0D); double d10 = (double)IIcon.func_94207_b(6.0D); double d11 =
		 * (double)IIcon.func_94214_a(9.0D); double d12 = (double)IIcon.func_94207_b(8.0D); double d13 =
		 * (double)IIcon.func_94214_a(7.0D); double d14 = (double)IIcon.func_94207_b(13.0D); double d15 =
		 * (double)IIcon.func_94214_a(9.0D); double d16 = (double)IIcon.func_94207_b(15.0D);
		 */

		final float var16 = IIcon.getMinU();
		final float var17 = IIcon.getMaxU();
		final float var18 = IIcon.getMinV();
		final float var19 = IIcon.getMaxV();
		/*
		 * float var16 = (float)var14 / 256.0F; float var17 = ((float)var14 + 15.99F) / 256.0F; float var18 =
		 * (float)var15 / 256.0F; float var19 = ((float)var15 + 15.99F) / 256.0F;
		 */
		final double var20b = offTexture.getInterpolatedU( 7.0D );
		final double var24b = offTexture.getInterpolatedU( 9.0D );

		final double var20 = IIcon.getInterpolatedU( 7.0D );
		final double var24 = IIcon.getInterpolatedU( 9.0D );
		final double var22 = IIcon.getInterpolatedV( 6.0D + ( isOn ? 0 : 1.0D ) );
		final double var26 = IIcon.getInterpolatedV( 8.0D + ( isOn ? 0 : 1.0D ) );
		final double var28 = IIcon.getInterpolatedU( 7.0D );
		final double var30 = IIcon.getInterpolatedV( 13.0D );
		final double var32 = IIcon.getInterpolatedU( 9.0D );
		final double var34 = IIcon.getInterpolatedV( 15.0D );

		final double var22b = IIcon.getInterpolatedV( 9.0D );
		final double var26b = IIcon.getInterpolatedV( 11.0D );

		baseX += 0.5D;
		baseZ += 0.5D;
		final double var36 = baseX - 0.5D;
		final double var38 = baseX + 0.5D;
		final double var40 = baseZ - 0.5D;
		final double var42 = baseZ + 0.5D;
		final double var44 = 0.0625D;
		final double var422 = 0.1915D + 1.0 / 16.0;
		final double TorchLen = 0.625D;

		double toff = 0.0d;

		if ( !isOn )
		{
			toff = 1.0d / 16.0d;
		}

		final Tessellator var12 = Tessellator.instance;
		if ( isOn )
		{
			var12.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
			var12.setBrightness( 11 << 20 | 11 << 4 );
		}

		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var20, var22 );
		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var20, var26 );
		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var24, var26 );
		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + TorchLen - toff, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var24, var22 );

		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var24b, var22b );
		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) + var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var24b, var26b );
		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) + var44, var20b, var26b );
		this.addVertexWithUV( baseX + Zero * ( 1.0D - TorchLen ) - var44, baseY + var422, baseZ + par10 * ( 1.0D - TorchLen ) - var44, var20b, var22b );

		this.addVertexWithUV( baseX + var44 + Zero, baseY, baseZ - var44 + par10, var32, var30 );
		this.addVertexWithUV( baseX + var44 + Zero, baseY, baseZ + var44 + par10, var32, var34 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY, baseZ + var44 + par10, var28, var34 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY, baseZ - var44 + par10, var28, var30 );

		this.addVertexWithUV( baseX - var44, baseY + 1.0D, var40, var16, var18 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY + 0.0D, var40 + par10, var16, var19 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY + 0.0D, var42 + par10, var17, var19 );
		this.addVertexWithUV( baseX - var44, baseY + 1.0D, var42, var17, var18 );

		this.addVertexWithUV( baseX + var44, baseY + 1.0D, var42, var16, var18 );
		this.addVertexWithUV( baseX + Zero + var44, baseY + 0.0D, var42 + par10, var16, var19 );
		this.addVertexWithUV( baseX + Zero + var44, baseY + 0.0D, var40 + par10, var17, var19 );
		this.addVertexWithUV( baseX + var44, baseY + 1.0D, var40, var17, var18 );

		this.addVertexWithUV( var36, baseY + 1.0D, baseZ + var44, var16, var18 );
		this.addVertexWithUV( var36 + Zero, baseY + 0.0D, baseZ + var44 + par10, var16, var19 );
		this.addVertexWithUV( var38 + Zero, baseY + 0.0D, baseZ + var44 + par10, var17, var19 );
		this.addVertexWithUV( var38, baseY + 1.0D, baseZ + var44, var17, var18 );

		this.addVertexWithUV( var38, baseY + 1.0D, baseZ - var44, var16, var18 );
		this.addVertexWithUV( var38 + Zero, baseY + 0.0D, baseZ - var44 + par10, var16, var19 );
		this.addVertexWithUV( var36 + Zero, baseY + 0.0D, baseZ - var44 + par10, var17, var19 );
		this.addVertexWithUV( var36, baseY + 1.0D, baseZ - var44, var17, var18 );
	}

	private boolean isLevelEmitterOn()
	{
		if ( Platform.isClient() )
		{
			return ( this.clientFlags & this.FLAG_ON ) == this.FLAG_ON;
		}

		return this.outputStrength > 0;
	}

	/**
	 * Calculates the redstone level and sets the internal strength as well as the outputStrength based on the
	 * RedstoneMode
	 * The parameter can be used to reset periodic data, currently it is used to set the item throughput rate back to a
	 * neutral setting.
	 * 
	 * @param reset true if a reset should be enforced
	 */
	private void calculateLevelEmitterStrength( boolean reset )
	{
		boolean flipState = this.getConfigManager().getSetting( Settings.REDSTONE_EMITTER ) == RedstoneMode.LOW_SIGNAL;
		int value = this.strength;

		if ( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			flipState = false;
			try
			{
				value = this.proxy.getCrafting().isRequesting( this.config.getAEStackInSlot( 0 ) ) ? 15 : 0;
			}
			catch ( final GridAccessException e )
			{
				// :P
			}

		}
		else if ( this.getInstalledUpgrades( Upgrades.SPEED ) > 0 )
		{
			// value = ( int ) this.reportingValue % 16;
			// if ( this.lastReportedDiffValue == 0 )
			// {
			// final int sign = this.reportingValue - this.strength < 0 ? -1 : 1;
			// value = this.strength == this.reportingValue ? this.strength : this.strength + 1 * sign;
			// }
			// else if ( this.lastReportedDiffValue > 0 )
			// {
			// value = this.strength < 15 ? this.strength + 1 : this.strength;
			// }
			// else
			// {
			// value = this.strength > 0 ? this.strength - 1 : this.strength;
			// }

			final int signValue = this.lastReportedDiffValue < 0 ? -1 : 1;
			final int signMode = this.rateMode == LevelEmitterRateMode.NEGATIVE ? -1 : 1;
			final int highestBit = 64 - Long.numberOfLeadingZeros( Math.abs( this.lastReportedDiffValue ) );
			final int maxValue = highestBit * signValue * signMode + ( ( int ) this.reportingValue % 16 );

			int result = ( int ) this.reportingValue % 16;

			if ( this.strength > maxValue )
			{
				result = this.strength - 1;
			}
			else if ( this.strength < maxValue )
			{
				result = this.strength + 1;
			}
			else
			{
				result = this.strength;
			}

			value = Math.max( 0, Math.min( 15, result ) );

			if ( reset )
			{
				this.lastReportedDiffValue = 0;
			}
		}
		else if ( this.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 )
		{
			value = this.reportingValue <= this.lastReportedValue ? 15 : ( int ) ( ( double ) this.lastReportedValue / this.reportingValue * 15 );
		}
		else
		{
			value = this.reportingValue < this.lastReportedValue + 1 ? 15 : 0;

		}

		this.strength = value;
		this.outputStrength = flipState ? 15 - value : value;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setTexture( this.is.getIconIndex() );
		// rh.setTexture( CableBusTextures.ItemPartLevelEmitterOn.getIcon() );

		// rh.setBounds( 2, 2, 14, 14, 14, 16 );
		// rh.renderBlock( x, y, z, renderer );

		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderBlock( x, y, z, renderer );

		renderer.renderAllFaces = true;

		final Tessellator tess = Tessellator.instance;
		tess.setBrightness( rh.getBlock().getMixedBrightnessForBlock( this.getHost().getTile().getWorldObj(), x, y, z ) );
		tess.setColorOpaque_F( 1.0F, 1.0F, 1.0F );

		this.renderTorchAtAngle( x, y, z );

		renderer.renderAllFaces = false;

		rh.setBounds( 7, 7, 11, 9, 9, 12 );
		this.renderLights( x, y, z, rh, renderer );

		// super.renderWorldBlock( world, x, y, z, block, modelId, renderer );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return this.is.getIconIndex();
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 7, 7, 11, 9, 9, 16 );
	}

	@Override
	public AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.SMART;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 16;
	}

	@Override
	public boolean canConnectRedstone()
	{
		return true;
	}

	@Override
	public int isProvidingStrongPower()
	{
		return this.prevState ? this.outputStrength : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return this.prevState ? this.outputStrength : 0;
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		if ( name.equals( "config" ) )
		{
			return this.config;
		}

		return super.getInventoryByName( name );
	}

	@Override
	public void onListUpdate()
	{
		try
		{
			this.updateReportingValue( this.proxy.getStorage().getItemInventory() );
		}
		catch ( final GridAccessException e )
		{
			// ;P
		}
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
		if ( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if ( this.settings.getSetting( Settings.CRAFT_VIA_REDSTONE ) == YesNo.YES )
			{
				final IAEItemStack what = this.config.getAEStackInSlot( 0 );
				if ( what != null )
				{
					craftingTracker.setEmitable( what );
				}
			}
		}
	}

	private void calculateEnergyRate()
	{
		try
		{
			this.lastReportedDiffValue = ( int ) ( this.proxy.getEnergy().getAvgPowerInjection() - this.proxy.getEnergy().getAvgPowerUsage() );
		}
		catch ( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( TickRates.LevelEmitter.min, TickRates.LevelEmitter.max, false, true );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		final boolean isRateUpgraded = this.getInstalledUpgrades( Upgrades.SPEED ) > 0;
		if ( isRateUpgraded )
		{
			if ( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
			{
				this.calculateEnergyRate();
			}

			this.calculateLevelEmitterStrength( true );
			final boolean speedDown = this.prevStrength == this.strength;

			this.updateState();
			return speedDown ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
		}

		return isRateUpgraded ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
	}
}
