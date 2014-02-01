package appeng.core.features;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.util.AEItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.core.Configuration;
import appeng.core.CreativeTab;
import appeng.core.CreativeTabFacade;
import appeng.items.parts.ItemFacade;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;

public class AEFeatureHandler implements AEItemDefinition
{

	private final EnumSet<AEFeature> myFeatures;

	private final String subname;
	private IAEFeature obj;

	private Item ItemData;
	private Block BlockData;
	private ItemStack StackData;

	public AEFeatureHandler(EnumSet<AEFeature> featureSet, IAEFeature _obj, String _subname) {
		myFeatures = featureSet;
		obj = _obj;
		subname = _subname;
	}

	public void register()
	{
		if ( isFeatureAvailable() )
		{
			if ( obj instanceof Item )
				initItem( (Item) obj );
			if ( obj instanceof Block )
				initBlock( (Block) obj );
		}
	}

	public static String getName(Class o, String subname)
	{
		String name = o.getSimpleName();

		if ( subname != null )
		{
			// simple hack to allow me to do get nice names for these without
			// mode code outside of AEBaseItem
			if ( subname.startsWith( "P2PTunnel" ) )
				return "ItemPart.P2PTunnel";

			if ( subname.equals( "CertusQuartzTools" ) )
				return name.replace( "Quartz", "CertusQuartz" );
			if ( subname.equals( "NetherQuartzTools" ) )
				return name.replace( "Quartz", "NetherQuartz" );

			name += "." + subname;
		}

		return name;
	}

	private void initItem(Item i)
	{
		ItemData = i;
		StackData = new ItemStack( i );

		String name = getName( i.getClass(), subname );
		i.setTextureName( "appliedenergistics2:" + name );
		i.setUnlocalizedName( /* "item." */"appliedenergistics2." + name );

		if ( i instanceof ItemFacade )
			i.setCreativeTab( CreativeTabFacade.instance );
		else
			i.setCreativeTab( CreativeTab.instance );

		GameRegistry.registerItem( i, "item." + name );
		AELog.localization( "item", i.getUnlocalizedName() );
	}

	private void initBlock(Block b)
	{
		BlockData = b;
		StackData = new ItemStack( b );

		String name = getName( b.getClass(), subname );
		b.setCreativeTab( CreativeTab.instance );
		b.setUnlocalizedName( /* "tile." */"appliedenergistics2." + name );
		b.setTextureName( "appliedenergistics2:" + name );

		if ( Platform.isClient() && BlockData instanceof AEBaseBlock )
		{
			AEBaseBlock bb = (AEBaseBlock) b;
			CommonHelper.proxy.bindTileEntitySpecialRenderer( bb.getTileEntityClass(), bb );
		}

		Class itemBlock = AEBaseItemBlock.class;
		if ( b instanceof AEBaseBlock )
			itemBlock = ((AEBaseBlock) b).getItemBlockClass();

		GameRegistry.registerBlock( b, itemBlock, "tile." + name );
		AELog.localization( "block", b.getUnlocalizedName() );
	}

	public EnumSet<AEFeature> getFeatures()
	{
		return myFeatures.clone();
	}

	public boolean isFeatureAvailable()
	{
		boolean enabled = true;

		for (AEFeature f : myFeatures)
			enabled = enabled && Configuration.instance.isFeatureEnabled( f );

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
		return ItemData;
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		if ( isFeatureAvailable() )
		{
			ItemStack rv = StackData.copy();
			rv.stackSize = stackSize;
			return rv;
		}
		return null;
	}

	@Override
	public boolean sameAs(ItemStack is)
	{
		if ( isFeatureAvailable() )
			return Platform.isSameItemType( is, StackData );
		return false;
	}

}
