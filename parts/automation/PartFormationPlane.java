package appeng.parts.automation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.AEConfig;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartFormationPlane extends PartUpgradeable implements ICellContainer, IPriorityHost, IMEInventory<IAEItemStack>
{

	int priority = 0;
	boolean wasActive = false;
	boolean blocked = false;
	MEInventoryHandler myHandler = new MEInventoryHandler( this, StorageChannel.ITEMS );
	AppEngInternalAEInventory Config = new AppEngInternalAEInventory( this, 63 );

	public PartFormationPlane(ItemStack is) {
		super( PartFormationPlane.class, is );
		settings.registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		updateHandler();
	}

	@Override
	public boolean onPartActivate(EntityPlayer player, Vec3 pos)
	{
		if ( !player.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			Platform.openGUI( player, getHost().getTile(), side, GuiBridge.GUI_FPLANE );
			return true;
		}

		return false;
	}

	protected int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "config" ) )
			return Config;

		return super.getInventoryByName( name );
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		boolean currentActive = proxy.isActive();
		if ( wasActive != currentActive )
		{
			wasActive = currentActive;
			updateHandler();// proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			getHost().markForUpdate();
		}
	}

	@MENetworkEventSubscribe
	public void updateChannels(MENetworkChannelsChanged chann)
	{
		boolean currentActive = proxy.isActive();
		if ( wasActive != currentActive )
		{
			wasActive = currentActive;
			updateHandler();// proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			getHost().markForUpdate();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), is.getIconIndex(), CableBusTextures.PartPlaneSides.getIcon(),
				CableBusTextures.PartPlaneSides.getIcon() );

		rh.setBounds( 1, 1, 15, 15, 15, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		ForgeDirection e = rh.getWorldX();
		ForgeDirection u = rh.getWorldY();

		TileEntity te = getHost().getTile();

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), side ) )
			minX = 0;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), side ) )
			maxX = 16;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), side ) )
			minY = 0;

		if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), side ) )
			maxY = 16;

		boolean isActive = (clientFlags & (POWERED_FLAG | CHANNEL_FLAG)) == (POWERED_FLAG | CHANNEL_FLAG);

		renderCache = rh.useSimplifiedRendering( x, y, z, this, renderCache );
		rh.setTexture( CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockFormPlaneOn.getIcon() : is.getIconIndex(),
				CableBusTextures.PartPlaneSides.getIcon(), CableBusTextures.PartPlaneSides.getIcon() );

		rh.setBounds( minX, minY, 15, maxX, maxY, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(),
				CableBusTextures.PartTransitionPlaneBack.getIcon(), isActive ? CableBusTextures.BlockFormPlaneOn.getIcon() : is.getIconIndex(),
				CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 14, 11, 11, 15 );
		rh.renderBlock( x, y, z, renderer );

		renderLights( x, y, z, rh, renderer );
	}

	private boolean isTransitionPlane(TileEntity blockTileEntity, ForgeDirection side)
	{
		if ( blockTileEntity instanceof IPartHost )
		{
			IPart p = ((IPartHost) blockTileEntity).getPart( side );
			return p instanceof PartFormationPlane;
		}
		return false;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		int minX = 1;
		int minY = 1;
		int maxX = 15;
		int maxY = 15;

		IPartHost host = getHost();
		if ( host != null )
		{
			TileEntity te = host.getTile();

			int x = te.xCoord;
			int y = te.yCoord;
			int z = te.zCoord;

			ForgeDirection e = bch.getWorldX();
			ForgeDirection u = bch.getWorldY();

			if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - e.offsetX, y - e.offsetY, z - e.offsetZ ), side ) )
				minX = 0;

			if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + e.offsetX, y + e.offsetY, z + e.offsetZ ), side ) )
				maxX = 16;

			if ( isTransitionPlane( te.getWorldObj().getTileEntity( x - u.offsetX, y - u.offsetY, z - u.offsetZ ), side ) )
				minY = 0;

			if ( isTransitionPlane( te.getWorldObj().getTileEntity( x + u.offsetX, y + u.offsetY, z + u.offsetZ ), side ) )
				maxY = 16;
		}

		bch.addBox( 5, 5, 14, 11, 11, 15 );
		bch.addBox( minX, minY, 15, maxX, maxY, 16 );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 1;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		if ( proxy.isActive() && channel == StorageChannel.ITEMS )
		{
			List<IMEInventoryHandler> Handler = new ArrayList( 1 );
			Handler.add( myHandler );
			return Handler;
		}
		return new ArrayList();
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		updateHandler();
		host.markForSave();
	}

	@Override
	public void setPriority(int newValue)
	{
		priority = newValue;
		host.markForSave();
		updateHandler();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		super.onChangeInventory( inv, slot, mc, removedStack, newStack );

		if ( inv == Config )
			updateHandler();
	}

	public void upgradesChanged()
	{
		updateHandler();
	}

	private void updateHandler()
	{
		myHandler.myAccess = AccessRestriction.WRITE;
		myHandler.myWhitelist = getInstalledUpgrades( Upgrades.INVERTER ) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;
		myHandler.myPriority = priority;

		IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

		int slotsToUse = 18 + getInstalledUpgrades( Upgrades.CAPACITY ) * 9;
		for (int x = 0; x < Config.getSizeInventory() && x < slotsToUse; x++)
		{
			IAEItemStack is = Config.getAEStackInSlot( x );
			if ( is != null )
				priorityList.add( is );
		}

		if ( getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
			myHandler.myPartitionList = new FuzzyPriorityList( priorityList, (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE ) );
		else
			myHandler.myPartitionList = new PrecisePriorityList( priorityList );

		try
		{
			proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		Config.writeToNBT( data, "config" );
		data.setInteger( "priority", priority );
	}

	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		Config.readFromNBT( data, "config" );
		priority = data.getInteger( "priority" );
		updateHandler();
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out)
	{
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void blinkCell(int slot)
	{
		// :P
	}

	@Override
	public void onNeighborChanged()
	{
		TileEntity te = host.getTile();
		World w = te.getWorldObj();
		ForgeDirection side = this.side;

		int x = te.xCoord + side.offsetX;
		int y = te.yCoord + side.offsetY;
		int z = te.zCoord + side.offsetZ;

		blocked = !w.getBlock( x, y, z ).isReplaceable( w, x, y, z );
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src)
	{
		if ( blocked || input == null || input.getStackSize() <= 0 )
			return input;

		ItemStack is = input.getItemStack();
		Item i = is.getItem();

		long maxStorage = Math.min( input.getStackSize(), is.getMaxStackSize() );
		boolean worked = false;

		TileEntity te = host.getTile();
		World w = te.getWorldObj();
		ForgeDirection side = this.side;

		int x = te.xCoord + side.offsetX;
		int y = te.yCoord + side.offsetY;
		int z = te.zCoord + side.offsetZ;

		if ( w.getBlock( x, y, z ).isReplaceable( w, x, y, z ) )
		{
			if ( i instanceof ItemBlock || i instanceof IPlantable || i instanceof ItemSkull || i instanceof ItemFirework || i instanceof IPartItem
					|| i instanceof ItemReed )
			{
				EntityPlayer player = Platform.getPlayer( (WorldServer) w );
				Platform.configurePlayer( player, side, tile );

				if ( i instanceof ItemFirework )
				{
					Chunk c = w.getChunkFromBlockCoords( x, z );
					int sum = 0;
					for (List Z : c.entityLists)
						sum += Z.size();
					if ( sum > 32 )
						return input;
				}
				maxStorage = is.stackSize;
				worked = true;
				if ( type == Actionable.MODULATE )
				{
					if ( i instanceof IPlantable || i instanceof ItemSkull || i instanceof ItemReed )
					{
						boolean Worked = false;

						if ( Worked == false && side.offsetX == 0 && side.offsetZ == 0 )
							Worked = i.onItemUse( is, player, w, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side.getOpposite().ordinal(),
									side.offsetX, side.offsetY, side.offsetZ );

						if ( Worked == false && side.offsetX == 0 && side.offsetZ == 0 )
							Worked = i.onItemUse( is, player, w, x - side.offsetX, y - side.offsetY, z - side.offsetZ, side.ordinal(), side.offsetX,
									side.offsetY, side.offsetZ );

						if ( Worked == false && side.offsetY == 0 )
							Worked = i.onItemUse( is, player, w, x, y - 1, z, ForgeDirection.UP.ordinal(), side.offsetX, side.offsetY, side.offsetZ );

						if ( Worked == false )
							Worked = i.onItemUse( is, player, w, x, y, z, side.getOpposite().ordinal(), side.offsetX, side.offsetY, side.offsetZ );

						maxStorage = maxStorage - is.stackSize;
					}
					else
					{
						i.onItemUse( is, player, w, x, y, z, side.getOpposite().ordinal(), side.offsetX, side.offsetY, side.offsetZ );
						maxStorage = maxStorage - is.stackSize;
					}
				}
				else
					maxStorage = 1;
			}
			else
			{
				worked = true;
				Chunk c = w.getChunkFromBlockCoords( x, z );
				int sum = 0;
				for (List Z : c.entityLists)
					sum += Z.size();

				if ( sum < AEConfig.instance.formationPlaneEntityLimit )
				{
					if ( type == Actionable.MODULATE )
					{

						is.stackSize = (int) maxStorage;
						EntityItem ei = new EntityItem( w, // w
								((side.offsetX != 0 ? 0.0 : 0.7) * (Platform.getRandomFloat() - 0.5f)) + 0.5 + side.offsetX * -0.3 + (double) x, // spawn
								((side.offsetY != 0 ? 0.0 : 0.7) * (Platform.getRandomFloat() - 0.5f)) + 0.5 + side.offsetY * -0.3 + (double) y, // spawn
								((side.offsetZ != 0 ? 0.0 : 0.7) * (Platform.getRandomFloat() - 0.5f)) + 0.5 + side.offsetZ * -0.3 + (double) z, // spawn
								is.copy() );

						Entity result = ei;

						ei.motionX = side.offsetX * 0.2;
						ei.motionY = side.offsetY * 0.2;
						ei.motionZ = side.offsetZ * 0.2;

						if ( is.getItem().hasCustomEntity( is ) )
						{
							result = is.getItem().createEntity( w, ei, is );
							if ( result != null )
								ei.setDead();
							else
								result = ei;
						}

						if ( !w.spawnEntityInWorld( result ) )
						{
							result.setDead();
							worked = false;
						}

					}
				}
				else
					worked = false;
			}
		}

		blocked = !w.getBlock( x, y, z ).isReplaceable( w, x, y, z );

		if ( worked )
		{
			IAEItemStack out = input.copy();
			out.decStackSize( maxStorage );
			if ( out.getStackSize() == 0 )
				return null;
			return out;
		}

		return input;
	}

	@Override
	public void saveChanges(IMEInventory cellInventory)
	{
		// nope!
	}
}
