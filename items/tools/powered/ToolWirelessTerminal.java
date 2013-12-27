package appeng.items.tools.powered;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.core.features.AEFeature;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;

public class ToolWirelessTerminal extends AEBasePoweredItem
{

	String name;
	int DISTNACE_CALC = 2;

	public ToolWirelessTerminal() {
		super( ToolWirelessTerminal.class, null );
		setfeature( EnumSet.of( AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools ) );
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player)
	{
		AEApi.instance().registries().wireless().OpenWirelessTermainlGui( item, w, player );
		return item;
	}

	@Override
	public void addInformation(ItemStack i, EntityPlayer p, List l, boolean b)
	{
		if ( i.hasTagCompound() )
		{
			NBTTagCompound tag = Platform.openNbtData( i );
			if ( tag != null )
			{
				String encKey = tag.getString( "encKey" );

				if ( encKey == null || encKey == "" )
					l.add( StatCollector.translateToLocal( "AppEng.GuiITooltip.Unlinked" ) );
				else
					l.add( StatCollector.translateToLocal( "AppEng.GuiITooltip.Linked" ) );
			}
		}
		else
			l.add( StatCollector.translateToLocal( "AppEng.GuiITooltip.Unlinked" ) );
	}

}
