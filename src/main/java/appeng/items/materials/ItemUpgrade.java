package appeng.items.materials;


import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.items.AEBaseItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemUpgrade extends AEBaseItem implements IUpgradeModule
{
	private final Upgrades type;

	public ItemUpgrade( Upgrades type, Properties properties )
	{
		super( properties );
		this.type = type;
	}

	@Override
	public Upgrades getType( ItemStack is )
	{
		return type;
	}

	@Override
	public void addInformation( @Nonnull ItemStack is, @Nullable World worldIn, @Nonnull List<ITextComponent> lines, @Nonnull ITooltipFlag flagIn )
	{
		super.addInformation( is, worldIn, lines, flagIn );

		final Set<String> textSet = new HashSet<>();
		for( final Map.Entry<ItemStack, Integer> entry : type.getSupported().entrySet() )
		{
			String name = null;

			final int limit = entry.getValue();

			if( entry.getKey().getItem() instanceof IItemGroup )
			{
				final IItemGroup ig = (IItemGroup) entry.getKey().getItem();
				final String str = ig.getUnlocalizedGroupName( type.getSupported().keySet(), entry.getKey() );
				if( str != null )
				{
					name = Platform.gui_localize( str ) + ( limit > 1 ? " (" + limit + ')' : "" );
				}
			}

			if( name == null )
			{
				name = entry.getKey().getDisplayName() + ( limit > 1 ? " (" + limit + ')' : "" );
			}

			textSet.add( name );
		}

		textSet.stream().sorted( SORT ).map( StringTextComponent::new ).forEach( lines::add );
	}

	private final Comparator<String> SORT = new Comparator<String>()
	{
		private final Pattern pattern = Pattern.compile( "(\\d+)[^\\d]" );

		@Override
		public int compare( final String o1, final String o2 )
		{
			try
			{
				final Matcher a = this.pattern.matcher( o1 );
				final Matcher b = this.pattern.matcher( o2 );
				if( a.find() && b.find() )
				{
					final int ia = Integer.parseInt( a.group( 1 ) );
					final int ib = Integer.parseInt( b.group( 1 ) );
					return Integer.compare( ia, ib );
				}
			}
			catch( final Throwable t )
			{
				// ek!
			}
			return o1.compareTo( o2 );
		}
	};

	@Override
	public ActionResultType onItemUseFirst( ItemStack stack, ItemUseContext context )
	{
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		if( player != null && player.isCrouching() )
		{
			final TileEntity te = context.getWorld().getTileEntity( context.getPos() );
			IItemHandler upgrades = null;

			if( te instanceof IPartHost )
			{
				final SelectedPart sp = ( (IPartHost) te ).selectPart( context.getHitVec() );
				if( sp.part instanceof IUpgradeableHost )
				{
					upgrades = ( (ISegmentedInventory) sp.part ).getInventoryByName( "upgrades" );
				}
			}
			else if( te instanceof IUpgradeableHost )
			{
				upgrades = ( (ISegmentedInventory) te ).getInventoryByName( "upgrades" );
			}

			if( upgrades != null && !player.getHeldItem( hand ).isEmpty() && player.getHeldItem( hand ).getItem() instanceof IUpgradeModule )
			{
				final IUpgradeModule um = (IUpgradeModule) player.getHeldItem( hand ).getItem();
				final Upgrades u = um.getType( player.getHeldItem( hand ) );

				if( u != null )
				{
					if( player.world.isRemote )
					{
						return ActionResultType.PASS;
					}

					final InventoryAdaptor ad = new AdaptorItemHandler( upgrades );
					player.setHeldItem( hand, ad.addItems( player.getHeldItem( hand ) ) );
					return ActionResultType.SUCCESS;
				}
			}
		}

		return super.onItemUseFirst( stack, context );
	}

}
