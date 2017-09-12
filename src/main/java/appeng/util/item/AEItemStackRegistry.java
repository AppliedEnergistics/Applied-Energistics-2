
package appeng.util.item;


import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;


public final class AEItemStackRegistry
{
	private static final WeakHashMap<AESharedItemStack, WeakReference<AESharedItemStack>> REGISTRY = new WeakHashMap<>();

	private AEItemStackRegistry()
	{
	}

	static synchronized AESharedItemStack getRegisteredStack( final @Nonnull ItemStack itemStack )
	{
		if( itemStack.isEmpty() )
		{
			throw new IllegalArgumentException( "stack cannot be empty" );
		}

		int oldStackSize = itemStack.getCount();
		itemStack.setCount( 1 );

		AESharedItemStack search = new AESharedItemStack( itemStack );
		WeakReference<AESharedItemStack> weak = REGISTRY.get( search );
		AESharedItemStack ret = null;

		if( weak != null )
		{
			ret = weak.get();
		}

		if( ret == null )
		{
			ret = new AESharedItemStack( itemStack.copy() );
			REGISTRY.put( ret, new WeakReference<>( ret ) );
		}
		itemStack.setCount( oldStackSize );

		return ret;
	}
}
