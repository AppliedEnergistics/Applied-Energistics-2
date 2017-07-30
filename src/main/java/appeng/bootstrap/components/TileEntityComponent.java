
package appeng.bootstrap.components;


import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.AppEng;


/**
 * @author GuntherDW
 */
public class TileEntityComponent implements IPreInitComponent
{
	private List<TileEntityDefinition> tileEntityDefinitions = new ArrayList<>();

	public TileEntityComponent()
	{
	}

	public void addTileEntity( TileEntityDefinition tileEntityDefinition )
	{
		if( !this.tileEntityDefinitions.contains( tileEntityDefinition ) )
		{
			this.tileEntityDefinitions.add( tileEntityDefinition );
		}
	}

	@Override
	public void preInitialize( Side side )
	{
		for( TileEntityDefinition tileEntityDefinition : this.tileEntityDefinitions )
		{
			if( !tileEntityDefinition.isRegistered() )
			{
				GameRegistry.registerTileEntity( tileEntityDefinition.getTileEntityClass(), AppEng.MOD_ID + ":" + tileEntityDefinition.getName() );
				tileEntityDefinition.setRegistered( true );
			}
		}
	}
}
