package appeng.util.item;

import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEItemDef
{

	public int myHash;

	public int def;

	private int itemID;
	public Item item;
	public int damageValue;

	public int displayDamage;
	public int maxDamage;

	public AESharedNBT tagCompound;

	@SideOnly(Side.CLIENT)
	public String displayName;

	@SideOnly(Side.CLIENT)
	public List tooltip;

	@SideOnly(Side.CLIENT)
	public UniqueIdentifier uniqueID;

	public OreReference isOre;

	static AESharedNBT lowTag = new AESharedNBT( Integer.MIN_VALUE );
	static AESharedNBT highTag = new AESharedNBT( Integer.MAX_VALUE );

	public AEItemDef(Item it) {
		item = it;
		itemID = System.identityHashCode( item );
	}

	public AEItemDef copy()
	{
		AEItemDef t = new AEItemDef( item );
		t.def = def;
		t.damageValue = damageValue;
		t.displayDamage = displayDamage;
		t.maxDamage = maxDamage;
		t.tagCompound = tagCompound;
		t.isOre = isOre;
		return t;
	}

	@Override
	public boolean equals(Object obj)
	{
		AEItemDef def = (AEItemDef) obj;
		return def.damageValue == damageValue && def.item == item && tagCompound == def.tagCompound;
	}

	public int getDamageValueHack(ItemStack is)
	{
		return Items.blaze_rod.getDamage( is );
	}

	public boolean isItem(ItemStack otherStack)
	{
		// hackery!
		int dmg = getDamageValueHack( otherStack );

		if ( item == otherStack.getItem() && dmg == damageValue )
		{
			if ( (tagCompound != null) == otherStack.hasTagCompound() )
				return true;

			if ( tagCompound != null && otherStack.hasTagCompound() )
				return Platform.NBTEqualityTest( (NBTBase) tagCompound, otherStack.getTagCompound() );

			return true;
		}
		return false;
	}

	public void reHash()
	{
		def = itemID << Platform.DEF_OFFSET | damageValue;
		myHash = def ^ (tagCompound == null ? 0 : System.identityHashCode( tagCompound ));
	}
}
