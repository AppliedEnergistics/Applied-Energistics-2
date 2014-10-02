package appeng.core.features;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.AEItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;
import appeng.core.CreativeTabFacade;
import appeng.items.parts.ItemFacade;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class AEFeatureHandler implements AEItemDefinition
{

	private final EnumSet<AEFeature> features;

	private final String subName;
	private final IAEFeature feature;

	private Item ItemData;
	private Block BlockData;

	public AEFeatureHandler(EnumSet<AEFeature> features, IAEFeature feature, String subName) {
		this.features = features;
		this.feature = feature;
		this.subName = subName;
	}

	public void register()
	{
		if ( isFeatureAvailable() )
		{
			if ( feature instanceof Item )
				initItem( (Item) feature );
			if ( feature instanceof Block )
				initBlock( (Block) feature );
		}
	}

	public static String getName(Class o, String subName)
	{
		String name = o.getSimpleName();

		if ( name.startsWith( "ItemMultiPart" ) )
			name = name.replace( "ItemMultiPart", "ItemPart" );
		else if ( name.startsWith( "ItemMultiMaterial" ) )
			name = name.replace( "ItemMultiMaterial", "ItemMaterial" );

		if ( subName != null )
		{
			// simple hack to allow me to do get nice names for these without
			// mode code outside of AEBaseItem
			if ( subName.startsWith( "P2PTunnel" ) )
				return "ItemPart.P2PTunnel";

			if ( subName.equals( "CertusQuartzTools" ) )
				return name.replace( "Quartz", "CertusQuartz" );
			if ( subName.equals( "NetherQuartzTools" ) )
				return name.replace( "Quartz", "NetherQuartz" );

			name += "." + subName;
		}

		return name;
	}

	private void initItem(Item i)
	{
		ItemData = i;

		String name = getName( i.getClass(), subName );
		i.setTextureName( "appliedenergistics2:" + name );
		i.setUnlocalizedName( /* "item." */"appliedenergistics2." + name );

		if ( i instanceof ItemFacade )
			i.setCreativeTab( CreativeTabFacade.instance );
		else
			i.setCreativeTab( CreativeTab.instance );

		if ( name.equals( "ItemMaterial" ) )
			name = "ItemMultiMaterial";
		else if ( name.equals( "ItemPart" ) )
			name = "ItemMultiPart";

		GameRegistry.registerItem( i, "item." + name );
	}

	private void initBlock(Block b)
	{
		BlockData = b;

		String name = getName( b.getClass(), subName );
		b.setCreativeTab( CreativeTab.instance );
		b.setBlockName( /* "tile." */"appliedenergistics2." + name );
		b.setBlockTextureName( "appliedenergistics2:" + name );

		if ( Platform.isClient() && BlockData instanceof AEBaseBlock )
		{
			AEBaseBlock bb = (AEBaseBlock) b;
			CommonHelper.proxy.bindTileEntitySpecialRenderer( bb.getTileEntityClass(), bb );
		}

		Class itemBlock = AEBaseItemBlock.class;
		if ( b instanceof AEBaseBlock )
			itemBlock = ((AEBaseBlock) b).getItemBlockClass();

		GameRegistry.registerBlock( b, itemBlock, "tile." + name );
	}

	public EnumSet<AEFeature> getFeatures()
	{
		return features.clone();
	}

	public boolean isFeatureAvailable()
	{
		boolean enabled = true;

		for (AEFeature f : features)
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

		return enabled;
	}

	@Override
	public Block block()
	{
		return BlockData;
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		if ( BlockData instanceof AEBaseBlock )
		{
			AEBaseBlock bb = (AEBaseBlock) BlockData;
			return bb.getTileEntityClass();
		}

		return null;
	}

	@Override
	public Item item()
	{
		if ( ItemData == null && BlockData != null )
			return Item.getItemFromBlock( BlockData );
		return ItemData;
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		if ( isFeatureAvailable() )
		{
			ItemStack rv = null;

			if ( ItemData != null )
				rv = new ItemStack( ItemData );
			else
				rv = new ItemStack( BlockData );

			rv.stackSize = stackSize;
			return rv;
		}
		return null;
	}

	@Override
	public boolean sameAsStack(ItemStack is)
	{
		if ( isFeatureAvailable() )
			return Platform.isSameItemType( is, stack( 1 ) );
		return false;
	}

	@Override
	public boolean sameAsBlock(IBlockAccess world, int x, int y, int z)
	{
		if ( isFeatureAvailable() && BlockData != null )
			return world.getBlock( x, y, z ) == block();
		return false;
	}

}
