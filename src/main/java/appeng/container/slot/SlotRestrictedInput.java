package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.items.misc.ItemEncodedPattern;
import appeng.recipes.handlers.Inscribe;
import appeng.util.Platform;

public class SlotRestrictedInput extends AppEngSlot
{

	public enum PlacableItemType
	{
		STORAGE_CELLS(15), ORE(1 * 16 + 15), STORAGE_COMPONENT(3 * 16 + 15),

		ENCODEABLE_ITEM(4 * 16 + 15), TRASH(5 * 16 + 15), VALID_ENCODED_PATTERN_W_OUTPUT(7 * 16 + 15), ENCODED_PATTERN_W_OUTPUT(7 * 16 + 15),

		ENCODED_CRAFTING_PATTERN(7 * 16 + 15), ENCODED_PATTERN(7 * 16 + 15), PATTERN(8 * 16 + 15), BLANK_PATTERN(8 * 16 + 15), POWERED_TOOL(9 * 16 + 15),

		RANGE_BOOSTER(6 * 16 + 15), QE_SINGULARITY(10 * 16 + 15), SPATIAL_STORAGE_CELLS(11 * 16 + 15),

		FUEL(12 * 16 + 15), UPGRADES(13 * 16 + 15), WORKBENCH_CELL(15), BIOMETRIC_CARD(14 * 16 + 15), VIEWCELL(4 * 16 + 14),

		INSCRIBER_PLATE(2 * 16 + 14), INSCRIBER_INPUT(3 * 16 + 14), METAL_INGOTS(3 * 16 + 14);

		public final int IIcon;

		private PlacableItemType(int o) {
			IIcon = o;
		}
	};

	@Override
	public int getSlotStackLimit()
	{
		if ( stackLimit != -1 )
			return stackLimit;
		return super.getSlotStackLimit();
	}

	public boolean isValid(ItemStack is, World theWorld)
	{
		if ( which == PlacableItemType.VALID_ENCODED_PATTERN_W_OUTPUT )
		{
			ICraftingPatternDetails ap = is.getItem() instanceof ICraftingPatternItem ? ((ICraftingPatternItem) is.getItem()).getPatternForItem( is, theWorld )
					: null;
			if ( ap != null )
				return true;
			return false;
		}
		return true;
	}

	public PlacableItemType which;
	public boolean allowEdit = true;
	public int stackLimit = -1;
	private InventoryPlayer p;

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return allowEdit;
	}

	public Slot setStackLimit(int i)
	{
		stackLimit = i;
		return this;
	}

	public SlotRestrictedInput(PlacableItemType valid, IInventory i, int slotnum, int x, int y, InventoryPlayer p) {
		super( i, slotnum, x, y );
		which = valid;
		IIcon = valid.IIcon;
		this.p = p;
	}

	@Override
	public ItemStack getDisplayStack()
	{
		if ( Platform.isClient() && (which == PlacableItemType.ENCODED_PATTERN) )
		{
			ItemStack is = super.getStack();
			if ( is != null && is.getItem() instanceof ItemEncodedPattern )
			{
				ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
				ItemStack out = iep.getOutput( is );
				if ( out != null )
					return out;
			}
		}
		return super.getStack();
	}

	@Override
	public boolean isItemValid(ItemStack i)
	{
		if ( !myContainer.isValidForSlot( this, i ) )
			return false;

		if ( i == null )
			return false;
		if ( i.getItem() == null )
			return false;

		if ( !inventory.isItemValidForSlot( this.getSlotIndex(), i ) )
			return false;

		IAppEngApi api = AEApi.instance();

		if ( !allowEdit )
			return false;

		switch (which)
		{
		case ENCODED_CRAFTING_PATTERN:
			if ( i.getItem() instanceof ICraftingPatternItem )
			{
				ICraftingPatternItem b = (ICraftingPatternItem) i.getItem();
				ICraftingPatternDetails de = b.getPatternForItem( i, p.player.worldObj );
				if ( de != null )
					return de.isCraftable();
			}
			return false;
		case VALID_ENCODED_PATTERN_W_OUTPUT:
		case ENCODED_PATTERN_W_OUTPUT:
		case ENCODED_PATTERN: {
			if ( i.getItem() instanceof ICraftingPatternItem )
				return true;
			// ICraftingPatternDetails pattern = i.getItem() instanceof ICraftingPatternItem ? ((ICraftingPatternItem)
			// i.getItem()).getPatternForItem( i ) : null;
			return false;// pattern != null;
		}
		case BLANK_PATTERN:
			return AEApi.instance().materials().materialBlankPattern.sameAsStack( i );
		case PATTERN:

			if ( i.getItem() instanceof ICraftingPatternItem )
				return true;

			return AEApi.instance().materials().materialBlankPattern.sameAsStack( i );

		case INSCRIBER_PLATE:

			if ( AEApi.instance().materials().materialNamePress.sameAsStack( i ) )
				return true;

			for (ItemStack is : Inscribe.plates)
				if ( Platform.isSameItemPrecise( is, i ) )
					return true;

			return false;

		case INSCRIBER_INPUT:
			return true;/*
						 * for (ItemStack is : Inscribe.inputs) if ( Platform.isSameItemPrecise( is, i ) ) return true;
						 * 
						 * return false;
						 */

		case METAL_INGOTS:

			return isMetalIngot( i );

		case VIEWCELL:
			return AEApi.instance().items().itemViewCell.sameAsStack( i );
		case ORE:
			return appeng.api.AEApi.instance().registries().grinder().getRecipeForInput( i ) != null;
		case FUEL:
			return TileEntityFurnace.getItemBurnTime( i ) > 0;
		case POWERED_TOOL:
			return Platform.isChargeable( i );
		case QE_SINGULARITY:
			return api.materials().materialQESingularity.sameAsStack( i );
		case RANGE_BOOSTER:
			return api.materials().materialWirelessBooster.sameAsStack( i );
		case SPATIAL_STORAGE_CELLS:
			return i.getItem() instanceof ISpatialStorageCell && ((ISpatialStorageCell) i.getItem()).isSpatialStorage( i );
		case STORAGE_CELLS:
			return AEApi.instance().registries().cell().isCellHandled( i );
		case WORKBENCH_CELL:
			return i != null && i.getItem() instanceof ICellWorkbenchItem && ((ICellWorkbenchItem) i.getItem()).isEditable( i );
		case STORAGE_COMPONENT:
			boolean isComp = i.getItem() instanceof IStorageComponent && ((IStorageComponent) i.getItem()).isStorageComponent( i );
			return isComp;
		case TRASH:
			if ( AEApi.instance().registries().cell().isCellHandled( i ) )
				return false;
			if ( i.getItem() instanceof IStorageComponent && ((IStorageComponent) i.getItem()).isStorageComponent( i ) )
				return false;
			return true;
		case ENCODEABLE_ITEM:
			return i.getItem() instanceof INetworkEncodable || AEApi.instance().registries().wireless().isWirelessTerminal( i );
		case BIOMETRIC_CARD:
			return i.getItem() instanceof IBiometricCard;
		case UPGRADES:
			return i.getItem() instanceof IUpgradeModule && ((IUpgradeModule) i.getItem()).getType( i ) != null;
		default:
			break;
		}

		return false;
	}

	static public boolean isMetalIngot(ItemStack i)
	{
		if ( Platform.isSameItemPrecise( i, new ItemStack( Items.iron_ingot ) ) )
			return true;

		for (String name : new String[] { "Copper", "Tin", "Obsidian", "Iron", "Lead", "Bronze", "Brass", "Nickel", "Aluminium" })
		{
			for (ItemStack ingot : OreDictionary.getOres( "ingot" + name ))
			{
				if ( Platform.isSameItemPrecise( i, ingot ) )
					return true;
			}
		}

		return false;
	}
}
