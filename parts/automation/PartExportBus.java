package appeng.parts.automation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartExportBus extends PartSharedItemBus implements IGridTickable, ICraftingRequester
{

	BaseActionSource mySrc;

	Future<ICraftingJob> calculatingJob = null;
	ICraftingLink[] links = null;

	public PartExportBus(ItemStack is) {
		super( PartExportBus.class, is );
		settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		settings.registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		settings.registerSetting( Settings.CRAFT_ONLY, YesNo.NO );
		mySrc = new MachineSource( this );
	}

	@Override
	public void readFromNBT(NBTTagCompound extra)
	{
		super.readFromNBT( extra );

		for (int x = 0; x < 9; x++)
		{
			NBTTagCompound link = extra.getCompoundTag( "links-" + x );
			if ( link != null && !link.hasNoTags() )
				setLink( x, AEApi.instance().storage().loadCraftingLink( link, this ) );
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound extra)
	{
		super.writeToNBT( extra );

		for (int x = 0; x < 9; x++)
		{
			ICraftingLink link = getLink( x );
			if ( link != null )
			{
				NBTTagCompound ln = new NBTTagCompound();
				link.writeToNBT( ln );
				extra.setTag( "links-" + x, ln );
			}
		}
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_BUS );
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{

		rh.setTexture( CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		renderCache = rh.useSimpliedRendering( x, y, z, this, renderCache );
		rh.setTexture( CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(),
				is.getIconIndex(), CableBusTextures.PartExportSides.getIcon(), CableBusTextures.PartExportSides.getIcon() );

		rh.setBounds( 4, 4, 12, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 6, 6, 15, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorBack.getIcon(), is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 6, 6, 11, 10, 10, 12 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 4, 4, 12, 12, 12, 14 );
		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( 6, 6, 15, 10, 10, 16 );
		bch.addBox( 6, 6, 11, 10, 10, 12 );
	}

	long itemToSend = 1;
	boolean didSomething = false;

	@Override
	TickRateModulation doBusWork()
	{
		if ( !proxy.isActive() )
			return TickRateModulation.IDLE;

		itemToSend = 1;
		didSomething = false;

		switch (getInstalledUpgrades( Upgrades.SPEED ))
		{
		default:
		case 0:
			itemToSend = 1;
			break;
		case 1:
			itemToSend = 8;
			break;
		case 2:
			itemToSend = 32;
			break;
		case 3:
			itemToSend = 64;
			break;
		case 4:
			itemToSend = 96;
			break;
		}

		try
		{
			InventoryAdaptor d = getHandler();
			IMEMonitor<IAEItemStack> inv = proxy.getStorage().getItemInventory();
			IEnergyGrid energy = proxy.getEnergy();
			FuzzyMode fzMode = (FuzzyMode) getConfigManager().getSetting( Settings.FUZZY_MODE );

			if ( d != null )
			{
				for (int x = 0; x < availableSlots() && itemToSend > 0; x++)
				{
					IAEItemStack ais = config.getAEStackInSlot( x );
					if ( ais == null || itemToSend <= 0 || craftOnly() )
					{
						handleCrafting( x, ais, d );
						continue;
					}

					long before = itemToSend;

					if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
					{
						for (IAEItemStack o : ImmutableList.copyOf( inv.getStorageList().findFuzzy( ais, fzMode ) ))
						{
							pushItemIntoTarget( d, energy, inv, o );
							if ( itemToSend <= 0 )
								break;
						}
					}
					else
						pushItemIntoTarget( d, energy, inv, ais );

					if ( itemToSend == before )
						handleCrafting( x, ais, d );
				}
			}

		}
		catch (GridAccessException e)
		{
			// :P
		}

		return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	private void handleCrafting(int x, IAEItemStack ais, InventoryAdaptor d) throws GridAccessException
	{
		if ( isCraftingEnabled() && ais != null && d.simulateAdd( ais.getItemStack() ) == null )
		{
			ICraftingGrid cg = proxy.getCrafting();

			if ( getLink( x ) != null )
			{
				return;
			}
			else if ( calculatingJob != null )
			{
				ICraftingJob job = null;
				try
				{
					if ( calculatingJob.isDone() )
						job = calculatingJob.get();
					else if ( calculatingJob.isCancelled() )
						calculatingJob = null;

					if ( job != null )
					{
						calculatingJob = null;
						setLink( x, cg.submitJob( job, this, null, mySrc ) );
						didSomething = true;
					}
				}
				catch (InterruptedException e)
				{
					// :P
				}
				catch (ExecutionException e)
				{
					// :P
				}
			}
			else
			{
				if ( getLink( x ) == null )
				{
					IAEItemStack aisC = ais.copy();
					aisC.setStackSize( itemToSend );
					calculatingJob = cg.beginCraftingJob( getTile().getWorldObj(), proxy.getGrid(), mySrc, aisC, null );
				}
			}
		}
	}

	private boolean craftOnly()
	{
		return getConfigManager().getSetting( Settings.CRAFT_ONLY ) == YesNo.YES;
	}

	private boolean isCraftingEnabled()
	{
		return getInstalledUpgrades( Upgrades.CRAFTING ) > 0;
	}

	private void pushItemIntoTarget(InventoryAdaptor d, IEnergyGrid energy, IMEInventory<IAEItemStack> inv, IAEItemStack ais)
	{
		ItemStack is = ais.getItemStack();
		is.stackSize = (int) itemToSend;

		ItemStack o = d.simulateAdd( is );
		long canFit = o == null ? itemToSend : itemToSend - o.stackSize;

		if ( canFit > 0 )
		{
			ais = ais.copy();
			ais.setStackSize( canFit );
			IAEItemStack itemsToAdd = Platform.poweredExtraction( energy, inv, ais, mySrc );

			if ( itemsToAdd != null )
			{
				itemToSend -= itemsToAdd.getStackSize();

				ItemStack failed = d.addItems( itemsToAdd.getItemStack() );
				if ( failed != null )
				{
					ais.setStackSize( failed.stackSize );
					inv.injectItems( ais, Actionable.MODULATE, mySrc );
				}
				else
					didSomething = true;
			}

		}
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return doBusWork();
	}

	public RedstoneMode getRSMode()
	{
		return (RedstoneMode) settings.getSetting( Settings.REDSTONE_CONTROLLED );
	}

	@Override
	protected boolean isSleeping()
	{
		return getHandler() == null || super.isSleeping();
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.ExportBus.min, TickRates.ExportBus.max, isSleeping(), false );
	}

	ICraftingLink getLink(int slot)
	{
		if ( links == null )
			return null;

		return links[slot];
	}

	void setLink(int slot, ICraftingLink l)
	{
		if ( links == null )
			links = new ICraftingLink[9];

		links[slot] = l;

		boolean hasStuff = false;
		for (int x = 0; x < links.length; x++)
		{
			ICraftingLink g = links[x];

			if ( g == null || g.isCanceled() || g.isDone() )
				links[x] = null;
			else
				hasStuff = true;
		}

		if ( hasStuff == false )
			links = null;
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		if ( links == null )
			return ImmutableSet.of();

		return ImmutableSet.copyOf( new NonNullArrayIterator( links ) );
	}

	@Override
	public IAEItemStack injectCratedItems(ICraftingLink link, IAEItemStack items)
	{
		InventoryAdaptor d = getHandler();

		try
		{
			if ( proxy.isActive() )
			{
				IEnergyGrid energy = proxy.getEnergy();

				double power = items.getStackSize();
				if ( energy.extractAEPower( power, Actionable.MODULATE, PowerMultiplier.CONFIG ) > power - 0.01 )
				{
					return AEItemStack.create( d.addItems( items.getItemStack() ) );
				}
			}
		}
		catch (GridAccessException e)
		{
			AELog.error( e );
		}

		return items;
	}

	@Override
	public void jobStateChange(ICraftingLink link)
	{
		if ( links != null )
		{
			for (int x = 0; x < links.length; x++)
			{
				if ( links[x] == link )
				{
					setLink( x, null );
					return;
				}
			}
		}
	}
}
