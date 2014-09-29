package appeng.container.implementations;

import appeng.api.config.*;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.parts.IPart;
import appeng.api.util.IConfigManager;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.contents.NetworkToolViewer;
import appeng.items.tools.ToolNetworkTool;
import appeng.parts.automation.PartExportBus;
import appeng.util.Platform;

public class ContainerUpgradeable extends AEBaseContainer implements IOptionalSlotHost
{

	IUpgradeableHost myte;

	int tbslot;
	NetworkToolViewer tbinv;

	public ContainerUpgradeable(InventoryPlayer ip, IUpgradeableHost te) {
		super( ip, (TileEntity) (te instanceof TileEntity ? te : null), (IPart) (te instanceof IPart ? te : null) );
		myte = te;

		World w = null;
		int xCoor = 0, yCoor = 0, zCoor = 0;

		if ( te instanceof TileEntity )
		{
			TileEntity myTile = (TileEntity) te;
			w = myTile.getWorldObj();
			xCoor = myTile.xCoord;
			yCoor = myTile.yCoord;
			zCoor = myTile.zCoord;
		}

		if ( te instanceof IPart )
		{
			IUpgradeableHost myTile = (IUpgradeableHost) te;
			TileEntity mk = myTile.getTile();
			w = mk.getWorldObj();
			xCoor = mk.xCoord;
			yCoor = mk.yCoord;
			zCoor = mk.zCoord;
		}

		IInventory pi = getPlayerInv();
		for (int x = 0; x < pi.getSizeInventory(); x++)
		{
			ItemStack pii = pi.getStackInSlot( x );
			if ( pii != null && pii.getItem() instanceof ToolNetworkTool )
			{
				lockPlayerInventorySlot( x );
				tbslot = x;
				tbinv = (NetworkToolViewer) ((ToolNetworkTool) pii.getItem()).getGuiObject( pii, w, xCoor, yCoor, zCoor );
				break;
			}
		}

		if ( hasToolbox() )
		{
			for (int v = 0; v < 3; v++)
				for (int u = 0; u < 3; u++)
					addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, tbinv, u + v * 3, 186 + u * 18, getHeight() - 82 + v * 18,
							invPlayer )).setPlayerSide() );
		}

		setupConfig();

		bindPlayerInventory( ip, 0, getHeight() - /* height of playerinventory */82 );
	}

	protected void setupUpgrades()
	{
		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		if ( availableUpgrades() > 0 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0, invPlayer )).setNotDraggable() );
		if ( availableUpgrades() > 1 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1, invPlayer )).setNotDraggable() );
		if ( availableUpgrades() > 2 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
		if ( availableUpgrades() > 3 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, invPlayer )).setNotDraggable() );
	}

	protected void setupConfig()
	{
		int x = 80;
		int y = 40;
		setupUpgrades();

		IInventory inv = myte.getInventoryByName( "config" );
		addSlotToContainer( new SlotFakeTypeOnly( inv, 0, x, y ) );

		if ( supportCapacity() )
		{
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 1, x, y, -1, 0, 1 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 2, x, y, 1, 0, 1 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 3, x, y, 0, -1, 1 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 4, x, y, 0, 1, 1 ) );

			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 5, x, y, -1, -1, 2 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 6, x, y, 1, -1, 2 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 7, x, y, -1, 1, 2 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 8, x, y, 1, 1, 2 ) );
		}
	}

	protected int getHeight()
	{
		return 184;
	}

	public int availableUpgrades()
	{
		return 4;
	}

	protected boolean supportCapacity()
	{
		return true;
	}

	@GuiSync(0)
	public RedstoneMode rsMode = RedstoneMode.IGNORE;

	@GuiSync(1)
	public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;

	@GuiSync(6)
	public ModMode mmMode = ModMode.FILTER_BY_ITEM;

	@GuiSync(5)
	public YesNo cMode = YesNo.NO;

	public void checkToolbox()
	{
		if ( hasToolbox() )
		{
			ItemStack currentItem = getPlayerInv().getStackInSlot( tbslot );

			if ( currentItem != tbinv.getItemStack() )
			{
				if ( currentItem != null )
				{
					if ( Platform.isSameItem( tbinv.getItemStack(), currentItem ) )
						getPlayerInv().setInventorySlotContents( tbslot, tbinv.getItemStack() );
					else
						isContainerValid = false;
				}
				else
					isContainerValid = false;
			}
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			IConfigManager cm = this.myte.getConfigManager();
			loadSettingsFromHost( cm );
		}

		checkToolbox();

		for (Object o : inventorySlots)
		{
			if ( o instanceof OptionalSlotFake )
			{
				OptionalSlotFake fs = (OptionalSlotFake) o;
				if ( !fs.isEnabled() && fs.getDisplayStack() != null )
					((OptionalSlotFake) fs).clearStack();
			}
		}

		standardDetectAndSendChanges();
	}

	protected void loadSettingsFromHost(IConfigManager cm)
	{
		this.fzMode = (FuzzyMode) cm.getSetting( Settings.FUZZY_MODE );
		this.mmMode = (ModMode) cm.getSetting( Settings.MOD_MODE );
		this.rsMode = (RedstoneMode) cm.getSetting( Settings.REDSTONE_CONTROLLED );
		if ( myte instanceof PartExportBus )
			this.cMode = (YesNo) cm.getSetting( Settings.CRAFT_ONLY );
	}

	protected void standardDetectAndSendChanges()
	{
		super.detectAndSendChanges();
	}

	public boolean hasToolbox()
	{
		return tbinv != null;
	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		int upgrades = myte.getInstalledUpgrades( Upgrades.CAPACITY );

		if ( idx == 1 && upgrades > 0 )
			return true;
		if ( idx == 2 && upgrades > 1 )
			return true;

		return false;
	}

}
