package appeng.util.item;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.features.IItemComparison;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;

/*
 * this is used for the shared NBT Cache.
 */
public class AESharedNBT extends NBTTagCompound implements IAETagCompound
{

	private int itemid, meta, hash;
	public SharedSearchObject sso;
	private IItemComparison comp;

	@Override
	public IItemComparison getSpecialComparison()
	{
		return comp;
	}

	private AESharedNBT(int itemID, int damageValue, String name) {
		super( name );
		itemid = itemID;
		meta = damageValue;
	}

	@Override
	public NBTTagCompound getNBTTagCompoundCopy()
	{
		return (NBTTagCompound) copy();
	}

	public static AESharedNBT createFromCompound(int itemID, int damageValue, NBTTagCompound c)
	{
		AESharedNBT x = new AESharedNBT( itemID, damageValue, c.getName() );

		Iterator var2 = c.getTags().iterator();

		while (var2.hasNext())
		{
			NBTBase tag = (NBTBase) var2.next();
			x.setTag( tag.getName(), (NBTBase) tag.copy() );
		}

		x.hash = Platform.NBTOrderlessHash( c );

		ItemStack isc = new ItemStack( itemID, 1, damageValue );
		isc.setTagCompound( c );
		x.comp = AEApi.instance().registries().specialComparson().getSpecialComparion( isc );

		return x;
	}

	@Override
	public boolean equals(Object par1Obj)
	{
		if ( par1Obj instanceof AESharedNBT )
			return this == par1Obj;
		return super.equals( par1Obj );
	}

	public boolean matches(int itemid2, int meta2, int orderlessHash)
	{
		return itemid2 == itemid && meta == meta2 && hash == orderlessHash;
	}

	public boolean comparePreciseWithRegistry(AESharedNBT tagCompound)
	{
		if ( this == tagCompound )
			return true;

		if ( comp != null && tagCompound.comp != null )
		{
			return comp.sameAsPrecise( tagCompound.comp );
		}

		return false;
	}

	public boolean compareFuzzyWithRegistry(AESharedNBT tagCompound)
	{
		if ( this == tagCompound )
			return true;
		if ( tagCompound == null )
			return false;

		if ( comp == tagCompound.comp )
			return true;

		if ( comp != null )
		{
			return comp.sameAsFuzzy( tagCompound.comp );
		}

		return false;
	}

	/*
	 * Shared Tag Compound Cache.
	 */
	private static WeakHashMap<SharedSearchObject, WeakReference<SharedSearchObject>> sharedTagCompounts = new WeakHashMap();

	/*
	 * Debug purposes.
	 */
	public static int sharedTagLoad()
	{
		return sharedTagCompounts.size();
	}

	/*
	 * returns true if the compound is part of the shared compound system ( and can thus be compared directly ).
	 */
	public static boolean isShared(NBTTagCompound ta)
	{
		return ta instanceof AESharedNBT;
	}

	/*
	 * Returns an NBT Compound that is used for accelerating comparisons.
	 */
	synchronized public static NBTTagCompound getSharedTagCompound(ItemStack s)
	{
		NBTTagCompound tagCompound = s.getTagCompound();
		if ( tagCompound == null )
			return null;
		if ( tagCompound.hasNoTags() )
			return null;

		int itemid = s.itemID;
		int meta = -1;
		if ( s.itemID != 0 && s.isItemStackDamageable() && s.getHasSubtypes() )
			meta = s.getItemDamage();

		if ( isShared( tagCompound ) )
			return tagCompound;

		SharedSearchObject sso = new SharedSearchObject( itemid, meta, tagCompound );

		WeakReference<SharedSearchObject> c = sharedTagCompounts.get( sso );
		if ( c != null )
		{
			SharedSearchObject cg = c.get();
			if ( cg != null )
				return cg.shared; // I don't think I really need to check this
									// as its already certain to exist..
		}

		AESharedNBT clone = AESharedNBT.createFromCompound( itemid, meta, tagCompound );
		sso.compound = (NBTTagCompound) sso.compound.copy(); // prevent
																// modification
																// of data based
																// on original
																// item.
		sso.shared = clone;
		clone.sso = sso;

		sharedTagCompounts.put( sso, new WeakReference<SharedSearchObject>( sso ) );
		return clone;
	}

}
