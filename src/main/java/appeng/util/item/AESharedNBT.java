package appeng.util.item;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

	private Item item;
	private int meta, hash;
	public SharedSearchObject sso;
	private IItemComparison comp;

	public int getHash()
	{
		return hash;
	}

	@Override
	public IItemComparison getSpecialComparison()
	{
		return comp;
	}

	private AESharedNBT(Item itemID, int damageValue) {
		super();
		item = itemID;
		meta = damageValue;
	}

	public AESharedNBT(int fakeValue) {
		super();
		item = null;
		meta = 0;
		hash = fakeValue;
	}

	@Override
	public NBTTagCompound getNBTTagCompoundCopy()
	{
		return (NBTTagCompound) copy();
	}

	public static AESharedNBT createFromCompound(Item itemID, int damageValue, NBTTagCompound c)
	{
		AESharedNBT x = new AESharedNBT( itemID, damageValue );

		// c.getTags()
		Iterator var2 = c.func_150296_c().iterator();

		while (var2.hasNext())
		{
			String name = (String) var2.next();
			x.setTag( name, c.getTag( name ).copy() );
		}

		x.hash = Platform.NBTOrderlessHash( c );

		ItemStack isc = new ItemStack( itemID, 1, damageValue );
		isc.setTagCompound( c );
		x.comp = AEApi.instance().registries().specialComparison().getSpecialComparison( isc );

		return x;
	}

	@Override
	public boolean equals(Object par1Obj)
	{
		if ( par1Obj instanceof AESharedNBT )
			return this == par1Obj;
		return super.equals( par1Obj );
	}

	public boolean matches(Item item, int meta, int orderlessHash)
	{
		return item == this.item && this.meta == meta && hash == orderlessHash;
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
	private static WeakHashMap<SharedSearchObject, WeakReference<SharedSearchObject>> sharedTagCompounds = new WeakHashMap();

	/*
	 * Debug purposes.
	 */
	public static int sharedTagLoad()
	{
		return sharedTagCompounds.size();
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
	synchronized public static NBTTagCompound getSharedTagCompound(NBTTagCompound tagCompound, ItemStack s)
	{
		if ( tagCompound.hasNoTags() )
			return null;

		Item item = s.getItem();
		int meta = -1;
		if ( s.getItem() != null && s.isItemStackDamageable() && s.getHasSubtypes() )
			meta = s.getItemDamage();

		if ( isShared( tagCompound ) )
			return tagCompound;

		SharedSearchObject sso = new SharedSearchObject( item, meta, tagCompound );

		WeakReference<SharedSearchObject> c = sharedTagCompounds.get( sso );
		if ( c != null )
		{
			SharedSearchObject cg = c.get();
			if ( cg != null )
				return cg.shared; // I don't think I really need to check this
									// as its already certain to exist..
		}

		AESharedNBT clone = AESharedNBT.createFromCompound( item, meta, tagCompound );
		sso.compound = (NBTTagCompound) sso.compound.copy(); // prevent
																// modification
																// of data based
																// on original
																// item.
		sso.shared = clone;
		clone.sso = sso;

		sharedTagCompounds.put( sso, new WeakReference<SharedSearchObject>( sso ) );
		return clone;
	}

}
