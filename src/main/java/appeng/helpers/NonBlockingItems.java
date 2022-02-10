package appeng.helpers;

import appeng.api.AEApi;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.util.item.AEItemStack;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.*;


public class NonBlockingItems
{
	public static Map<String, IItemList<IAEItemStack>> NON_BLOCKING_MAP = new HashMap<>();
	public static NonBlockingItems INSTANCE = new NonBlockingItems();

	private NonBlockingItems()
	{
		String[] strings = AEConfig.instance().getNonBlockingItems();
		String modid = "";
		if( strings.length > 0 )
		{
			for( String s : strings )
			{
				if( s.startsWith( "[" ) && s.endsWith( "]" ) )
				{
					modid = s.substring( 1, s.length() - 1 );
				}
				else
				{
					if( !Loader.isModLoaded( modid ) )
					{
						continue;
					}
					NON_BLOCKING_MAP.putIfAbsent( modid, AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList() );
					String[] ModItemMeta = s.split( ":" );

					if( ModItemMeta.length < 2 || ModItemMeta.length > 3 )
					{
						AELog.error( "Invalid non blocking item entry: " + s );
						continue;
					}

					if( ModItemMeta[0].equals( "gregtech" ) )
					{
						for( MetaItem<?> metaItem : MetaItem.getMetaItems() )
						{
							MetaItem<?>.MetaValueItem metaItem2 = metaItem.getItem( ModItemMeta[1] );
							if( metaItem.getItem( ModItemMeta[1] ) != null )
							{
								NON_BLOCKING_MAP.get( modid ).add( AEItemStack.fromItemStack( metaItem2.getStackForm() ) );
								break;
							}
						}
					}
					else
					{
						ItemStack itemStack = GameRegistry.makeItemStack( ModItemMeta[0] + ":" + ModItemMeta[1], ModItemMeta.length == 3 ? Integer.parseInt( ModItemMeta[2] ) : 0, 1, null );
						if( !itemStack.isEmpty() )
						{
							NON_BLOCKING_MAP.get( modid ).add( AEItemStack.fromItemStack( itemStack ) );
						}
						else
						{
							AELog.error( "Item not found on nonBlocking config: " + s );
						}
					}
				}
			}
		}
	}

	public Map<String, IItemList<IAEItemStack>> getMap()
	{
		return NON_BLOCKING_MAP;
	}
}
