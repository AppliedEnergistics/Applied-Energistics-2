package appeng.items.storage;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.MaterialType;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public class ItemBasicStorageCell extends AEBaseItem implements IStorageCell, IItemGroup
{

	final MaterialType component;
	final int totalBytes;
	final int perType;
	final double idleDrain;

	public ItemBasicStorageCell(MaterialType whichCell, int Kilobytes) {
		super( ItemBasicStorageCell.class, Kilobytes + "k" );
		setFeature( EnumSet.of( AEFeature.StorageCells ) );
		setMaxStackSize( 1 );
		totalBytes = Kilobytes * 1024;
		component = whichCell;

		switch (component)
		{
		case Cell1kPart:
			idleDrain = 0.5;
			perType = 8;
			break;
		case Cell4kPart:
			idleDrain = 1.0;
			perType = 32;
			break;
		case Cell16kPart:
			idleDrain = 1.5;
			perType = 128;
			break;
		case Cell64kPart:
			idleDrain = 2.0;
			perType = 512;
			break;
		default:
			idleDrain = 0.0;
			perType = 8;
		}
	}

	@Override
	public void addInformation(ItemStack i, EntityPlayer p, List l, boolean b)
	{
		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( i, null, StorageChannel.ITEMS );

		if ( cdi instanceof ICellInventoryHandler )
		{
			ICellInventoryHandler CI = (ICellInventoryHandler) cdi;
			
			ICellInventory cd = ((ICellInventoryHandler) cdi).getCellInv();
			if (cd != null)
			{
				l.add(cd.getUsedBytes() + " " + GuiText.Of.getLocal() + " "
						+ cd.getTotalBytes() + " "
						+ GuiText.BytesUsed.getLocal());
				
				l.add(cd.getStoredItemTypes() + " " + GuiText.Of.getLocal()
						+ " " + cd.getTotalItemTypes() + " "
						+ GuiText.Types.getLocal());
				
				if ( CI.isPreformatted() )
				{
					String List = (CI.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
									: GuiText.Excluded ).getLocal();
					
					if ( CI.isFuzzy() )
						l.add( GuiText.Partitioned.getLocal() + " - " + List + " " + GuiText.Fuzzy.getLocal() );
					else
						l.add( GuiText.Partitioned.getLocal() + " - " + List + " " + GuiText.Precise.getLocal()  );
					
				}
			}
		}
	}

	@Override
	public int getBytes(ItemStack cellItem) {
		return totalBytes;
	}

	@Override
	public int BytePerType(ItemStack iscellItem)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 63;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition)
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell(ItemStack i)
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return idleDrain;
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is)
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory(ItemStack is)
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is)
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch (Throwable t)
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode)
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public String getUnlocalizedGroupName(Set<ItemStack> others, ItemStack is)
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable(ItemStack is)
	{
		return true;
	}

	private boolean dissassembleDrive(ItemStack stack, World world, EntityPlayer player)
	{
		if ( player.isSneaking() )
		{
			if ( Platform.isClient() )
				return false;

			InventoryPlayer pinv = player.inventory;
			IMEInventory<IAEItemStack> inv = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );
			if ( inv != null && pinv.getCurrentItem() == stack )
			{
				InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
				IItemList<IAEItemStack> list = inv.getAvailableItems( StorageChannel.ITEMS.createList() );
				if ( list.isEmpty() && ia != null )
				{
					pinv.setInventorySlotContents( pinv.currentItem, null );

					ItemStack extraB = ia.addItems( component.stack( 1 ) );
					ItemStack extraA = ia.addItems( AEApi.instance().materials().materialEmptyStorageCell.stack( 1 ) );

					if ( extraA != null )
						player.dropPlayerItemWithRandomChoice( extraA, false );
					if ( extraB != null )
						player.dropPlayerItemWithRandomChoice( extraB, false );

					if ( player.inventoryContainer != null )
						player.inventoryContainer.detectAndSendChanges();

					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		dissassembleDrive( stack, world, player );
		return stack;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return dissassembleDrive( stack, world, player );
	}

	@Override
	public boolean hasContainerItem()
	{
		return AEConfig.instance.isFeatureEnabled( AEFeature.enableDisassemblyCrafting );
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		return AEApi.instance().materials().materialEmptyStorageCell.stack( 1 );
	}

}
