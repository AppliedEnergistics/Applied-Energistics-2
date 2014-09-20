package appeng.debug;

import java.util.EnumSet;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import appeng.client.texture.MissingIcon;
import appeng.core.features.AEFeature;
import appeng.helpers.MeteoritePlacer;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ToolMeteoritePlacer extends AEBaseItem
{

	public ToolMeteoritePlacer() {
		super( ToolMeteoritePlacer.class );
		setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isClient() )
			return false;

		MeteoritePlacer mp = new MeteoritePlacer();
		boolean worked = mp.spawnMeteorite( new MeteoritePlacer.StandardWorld( world ), x, y, z );

		if ( !worked )
			player.addChatMessage( new ChatComponentText( "Un-suiteable Location." ) );

		return true;
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		itemIcon = new MissingIcon( this );
	}

}
