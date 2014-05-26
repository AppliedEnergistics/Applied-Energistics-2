package appeng.items.misc;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.api.util.AEColor;
import appeng.client.render.items.PaintBallRender;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ItemPaintBall extends AEBaseItem
{

	public ItemPaintBall() {
		super( ItemPaintBall.class );
		setfeature( EnumSet.of( AEFeature.PaintBalls ) );
		hasSubtypes = true;
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, new PaintBallRender() );
	}

	@Override
	public String getItemStackDisplayName(ItemStack is)
	{
		return super.getItemStackDisplayName( is ) + " - " + getExtraName( is );
	}

	public String getExtraName(ItemStack is)
	{
		return (is.getItemDamage() >= 20 ? GuiText.Lumen + " " : "") + getColor( is );
	}

	public AEColor getColor(ItemStack is)
	{
		int dmg = is.getItemDamage();
		if ( dmg >= 20 )
			dmg -= 20;

		if ( dmg >= AEColor.values().length )
			return AEColor.Transparent;

		return AEColor.values()[dmg];
	}

	@Override
	public void getSubItems(Item i, CreativeTabs ct, List l)
	{
		for (AEColor c : AEColor.values())
			if ( c != AEColor.Transparent )
				l.add( new ItemStack( this, 1, c.ordinal() ) );

		for (AEColor c : AEColor.values())
			if ( c != AEColor.Transparent )
				l.add( new ItemStack( this, 1, 20 + c.ordinal() ) );
	}

}
