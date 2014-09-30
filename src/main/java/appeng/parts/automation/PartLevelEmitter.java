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
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartLevelEmitter extends PartUpgradeable implements IEnergyWatcherHost, IStackWatcherHost, ICraftingWatcherHost,
		IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingProvider
{

	final int FLAG_ON = 4;

	AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 1 );

	boolean prevState = false;

	long lastReportedValue = 0;
	long reportingValue = 0;

	IStackWatcher myWatcher;
	IEnergyWatcher myEnergyWatcher;
	ICraftingWatcher myCraftingWatcher;

	public long getReportingValue()
	{
		return reportingValue;
	}

	public void setReportingValue(long v)
	{
		reportingValue = v;
		if ( getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
			configureWatchers();
		else
			updateState();
	}

	@MENetworkEventSubscribe
	public void powerChanged(MENetworkPowerStatusChange c)
	{
		updateState();
	}

	@MENetworkEventSubscribe
	public void channelChanged(MENetworkChannelsChanged c)
	{
		updateState();
	}

	@Override
	public void upgradesChanged()
	{
		configureWatchers();
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		configureWatchers();
	}

	private void updateState()
	{
		if ( proxy.isActive() )
		{
			boolean isOn = isLevelEmitterOn();
			if ( prevState != isOn )
			{
				host.markForUpdate();
				TileEntity te = host.getTile();
				prevState = isOn;
				Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord );
				Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ );
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		data.setLong( "lastReportedValue", lastReportedValue );
		data.setLong( "reportingValue", reportingValue );
		data.setBoolean( "prevState", prevState );
		config.writeToNBT( data, "config" );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		lastReportedValue = data.getLong( "lastReportedValue" );
		reportingValue = data.getLong( "reportingValue" );
		prevState = data.getBoolean( "prevState" );
		config.readFromNBT( data, "config" );
	}

	@Override
	protected int populateFlags(int cf)
	{
		return cf | (prevState ? FLAG_ON : 0);
	}

	@Override
	public void updateWatcher(ICraftingWatcher newWatcher)
	{
		myCraftingWatcher = newWatcher;
		configureWatchers();
	}

	@Override
	public void updateWatcher(IStackWatcher newWatcher)
	{
		myWatcher = newWatcher;
		configureWatchers();
	}

	@Override
	public void updateWatcher(IEnergyWatcher newWatcher)
	{
		myEnergyWatcher = newWatcher;
		configureWatchers();
	}

	// update the system...
	public void configureWatchers()
	{
		IAEItemStack myStack = config.getAEStackInSlot( 0 );

		if ( myWatcher != null )
			myWatcher.clear();

		if ( myEnergyWatcher != null )
			myEnergyWatcher.clear();

		if ( myCraftingWatcher != null )
			myCraftingWatcher.clear();

		try
		{
			proxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, proxy.getNode() ) );
		}
		catch (GridAccessException e1)
		{
			// :/
		}

		if ( getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if ( myCraftingWatcher != null && myStack != null )
				myCraftingWatcher.add( myStack );

			return;
		}

		if ( getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			if ( myEnergyWatcher != null )
				myEnergyWatcher.add( (double) reportingValue );

			try
			{
				// update to power...
				lastReportedValue = (long) proxy.getEnergy().getStoredPower();
				updateState();

				// no more item stuff..
				proxy.getStorage().getItemInventory().removeListener( this );
			}
			catch (GridAccessException e)
			{
				// :P
			}

			return;
		}

		try
		{
			if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 || myStack == null )
			{
				proxy.getStorage().getItemInventory().addListener( this, proxy.getGrid() );
			}
			else
			{
				proxy.getStorage().getItemInventory().removeListener( this );

				if ( myWatcher != null )
					myWatcher.add( myStack );
			}

			updateReportingValue( proxy.getStorage().getItemInventory() );
		}
		catch (GridAccessException e)
		{
			// >.>
		}
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		if ( inv == config )
			configureWatchers();

		super.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, BaseActionSource actionSource)
	{
		updateReportingValue( (IMEMonitor<IAEItemStack>) monitor );
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_LEVEL_EMITTER );
			return true;
		}

		return false;
	}

	private void updateReportingValue(IMEMonitor<IAEItemStack> monitor)
	{
		IAEItemStack myStack = config.getAEStackInSlot( 0 );

		if ( myStack == null )
		{
			lastReportedValue = 0;
			for (IAEItemStack st : monitor.getStorageList())
				lastReportedValue += st.getStackSize();
		}
		else if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
		{
			lastReportedValue = 0;
			FuzzyMode fzMode = (FuzzyMode) getConfigManager().getSetting( Settings.FUZZY_MODE );
			Collection<IAEItemStack> fuzzyList = monitor.getStorageList().findFuzzy( myStack, fzMode );
			for (IAEItemStack st : fuzzyList)
				lastReportedValue += st.getStackSize();
		}
		else
		{
			IAEItemStack r = monitor.getStorageList().findPrecise( myStack );
			if ( r == null )
				lastReportedValue = 0;
			else
				lastReportedValue = r.getStackSize();
		}

		updateState();
	}

	@Override
	public boolean isValid(Object effectiveGrid)
	{
		try
		{
			return proxy.getGrid() == effectiveGrid;
		}
		catch (GridAccessException e)
		{
			return false;
		}
	}

	@Override
	public void onRequestChange(ICraftingGrid craftingGrid, IAEItemStack what)
	{
		updateState();
	}

	@Override
	public void onThresholdPass(IEnergyGrid energyGrid)
	{
		lastReportedValue = (long) energyGrid.getStoredPower();
		updateState();
	}

	@Override
	public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan)
	{
		if ( chan == StorageChannel.ITEMS && fullStack.equals( config.getAEStackInSlot( 0 ) ) && getInstalledUpgrades( Upgrades.FUZZY ) == 0 )
		{
			lastReportedValue = fullStack.getStackSize();
			updateState();
		}
	}

	public PartLevelEmitter(ItemStack is) {
		super( PartLevelEmitter.class, is );
		getConfigManager().registerSetting( Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL );
		getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		getConfigManager().registerSetting( Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL );
		getConfigManager().registerSetting( Settings.CRAFT_VIA_REDSTONE, YesNo.NO );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( is.getIconIndex() );
		Tessellator.instance.startDrawingQuads();
		renderTorchAtAngle( 0, -0.5, 0 );
		Tessellator.instance.draw();
		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderInventoryBox( renderer );
	}

	double centerX;
	double centerY;
	double centerZ;

	public void addVertexWithUV(double x, double y, double z, double u, double v)
	{
		Tessellator var12 = Tessellator.instance;

		x -= centerX;
		y -= centerY;
		z -= centerZ;

		if ( side == ForgeDirection.DOWN )
		{
			y = -y;
			z = -z;
		}

		if ( side == ForgeDirection.EAST )
		{
			double m = x;
			x = y;
			y = m;
			y = -y;
		}

		if ( side == ForgeDirection.WEST )
		{
			double m = x;
			x = -y;
			y = m;
		}

		if ( side == ForgeDirection.SOUTH )
		{
			double m = z;
			z = y;
			y = m;
			y = -y;
		}

		if ( side == ForgeDirection.NORTH )
		{
			double m = z;
			z = -y;
			y = m;
		}

		x += centerX;// + orientation.offsetX * 0.4;
		y += centerY;// + orientation.offsetY * 0.4;
		z += centerZ;// + orientation.offsetZ * 0.4;

		var12.addVertexWithUV( x, y, z, u, v );
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
		if ( isLevelEmitterOn() )
		{
			ForgeDirection d = side;

			double d0 = d.offsetX * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			double d1 = d.offsetY * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			double d2 = d.offsetZ * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;

			world.spawnParticle( "reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );
		}
	}

	public void renderTorchAtAngle(double baseX, double baseY, double baseZ)
	{
		boolean isOn = isLevelEmitterOn();
		IIcon offTexture = is.getIconIndex();
		IIcon IIcon = (isOn ? CableBusTextures.LevelEmitterTorchOn.getIcon() : offTexture);
		//
		centerX = baseX + 0.5;
		centerY = baseY + 0.5;
		centerZ = baseZ + 0.5;

		baseY += 7.0 / 16.0;

		double par10 = 0;
		// double par11 = 0;
		double Zero = 0;

		/*
		 * double d5 = (double)IIcon.func_94209_e(); double d6 = (double)IIcon.func_94206_g(); double d7 =
		 * (double)IIcon.func_94212_f(); double d8 = (double)IIcon.func_94210_h(); double d9 =
		 * (double)IIcon.func_94214_a(7.0D); double d10 = (double)IIcon.func_94207_b(6.0D); double d11 =
		 * (double)IIcon.func_94214_a(9.0D); double d12 = (double)IIcon.func_94207_b(8.0D); double d13 =
		 * (double)IIcon.func_94214_a(7.0D); double d14 = (double)IIcon.func_94207_b(13.0D); double d15 =
		 * (double)IIcon.func_94214_a(9.0D); double d16 = (double)IIcon.func_94207_b(15.0D);
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
		double var22 = IIcon.getInterpolatedV( 6.0D + (isOn ? 0 : 1.0D) );
		double var26 = IIcon.getInterpolatedV( 8.0D + (isOn ? 0 : 1.0D) );
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

		if ( !isOn )
		{
			toff = 1.0d / 16.0d;
		}

		Tessellator var12 = Tessellator.instance;
		if ( isOn )
		{
			var12.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
			var12.setBrightness( 11 << 20 | 11 << 4 );
		}

		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) - var44, var20, var22 );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) + var44, var20, var26 );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) + var44, var24, var26 );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) - var44, var24, var22 );

		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) - var44, var24b, var22b );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) + var44, var24b, var26b );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) + var44, var20b, var26b );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) - var44, var20b, var22b );

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

	boolean status = false;

	private boolean isLevelEmitterOn()
	{
		if ( Platform.isClient() )
			return (clientFlags & FLAG_ON) == FLAG_ON;

		if ( getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			try
			{
				return proxy.getCrafting().isRequesting( config.getAEStackInSlot( 0 ) );
			}
			catch (GridAccessException e)
			{
				// :P
			}

			return prevState;
		}

		boolean flipState = getConfigManager().getSetting( Settings.REDSTONE_EMITTER ) == RedstoneMode.LOW_SIGNAL;
		return flipState ? reportingValue >= lastReportedValue + 1 : reportingValue < lastReportedValue + 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( is.getIconIndex() );
		// rh.setTexture( CableBusTextures.ItemPartLevelEmitterOn.getIcon() );

		// rh.setBounds( 2, 2, 14, 14, 14, 16 );
		// rh.renderBlock( x, y, z, renderer );

		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderBlock( x, y, z, renderer );

		renderer.renderAllFaces = true;

		Tessellator tess = Tessellator.instance;
		tess.setBrightness( rh.getBlock().getMixedBrightnessForBlock( this.getHost().getTile().getWorldObj(), x, y, z ) );
		tess.setColorOpaque_F( 1.0F, 1.0F, 1.0F );

		renderTorchAtAngle( x, y, z );

		renderer.renderAllFaces = false;

		rh.setBounds( 7, 7, 11, 9, 9, 12 );
		renderLights( x, y, z, rh, renderer );

		// super.renderWorldBlock( world, x, y, z, block, modelId, renderer );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return is.getIconIndex();
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 7, 7, 11, 9, 9, 16 );
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
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
		return prevState ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return prevState ? 15 : 0;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "config" ) )
			return config;

		return super.getInventoryByName( name );
	}

	@Override
	public void onListUpdate()
	{
		try
		{
			updateReportingValue( proxy.getStorage().getItemInventory() );
		}
		catch (GridAccessException e)
		{
			// ;P
		}
	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table)
	{
		return false;
	}

	@Override
	public boolean isBusy()
	{
		return true;
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker)
	{
		if ( getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if ( settings.getSetting( Settings.CRAFT_VIA_REDSTONE ) == YesNo.YES )
			{
				IAEItemStack what = config.getAEStackInSlot( 0 );
				if ( what != null )
					craftingTracker.setEmitable( what );
			}
		}
	}
}
