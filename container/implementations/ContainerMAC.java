package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotMACPattern;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.items.misc.ItemEncodedPattern;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;

public class ContainerMAC extends ContainerUpgradeable
{

	TileMolecularAssembler tma;

	public ContainerMAC(InventoryPlayer ip, TileMolecularAssembler te) {
		super( ip, te );
		tma = te;
	}

	public int availableUpgrades()
	{
		return 5;
	}

	@Override
	protected int getHeight()
	{
		return 197;
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	@GuiSync(4)
	public int craftProgress = 0;

	public boolean isValidItemForSlot(int slotIndex, ItemStack i)
	{
		IInventory mac = myte.getInventoryByName( "mac" );

		ItemStack is = mac.getStackInSlot( 10 );
		if ( is == null )
			return false;

		if ( is.getItem() instanceof ItemEncodedPattern )
		{
			World w = this.getTileEntity().getWorldObj();
			ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
			ICraftingPatternDetails ph = iep.getPatternForItem( is, w );
			if ( ph.isCraftable() )
				return ph.isValidItemForSlot( slotIndex, i, w );
		}

		return false;
	}

	@Override
	protected void setupConfig()
	{
		int offx = 29;
		int offy = 30;

		IInventory mac = myte.getInventoryByName( "mac" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
			{
				SlotMACPattern s = new SlotMACPattern( this, mac, x + y * 3, offx + x * 18, offy + y * 18 );
				addSlotToContainer( s );
			}

		offx = 126;
		offy = 16;

		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.ENCODED_CRAFTING_PATTERN, mac, 10, offx, offy, invPlayer ) );
		addSlotToContainer( new SlotOutput( mac, 9, offx, offy + 32, -1 ) );

		offx = 122;
		offy = 17;

		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, invPlayer )).setNotDraggable() );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.rsMode = (RedstoneMode) this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
		}

		craftProgress = this.tma.getCraftingProgress();

		standardDetectAndSendChanges();
	}

}
