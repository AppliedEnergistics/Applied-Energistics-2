package appeng.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class FacadeConfig extends Configuration
{

	public static FacadeConfig instance;
	Pattern replacementPattern;

	public FacadeConfig(File f) {
		super( new File( f.getPath() + File.separator + "AppliedEnergistics2" + File.separator + "Facades.cfg" ) );
		replacementPattern = Pattern.compile( "[^a-zA-Z0-9]" );
	}

	public boolean checkEnabled(Block id, boolean automatic)
	{
		if ( id == null )
			return false;

		UniqueIdentifier blk = GameRegistry.findUniqueIdentifierFor( id );
		if ( blk == null )
		{
			for (Field f : Block.class.getFields())
			{
				try
				{
					if ( f.get( Block.class ) == id )
						return get( "minecraft", f.getName(), automatic ).getBoolean( automatic );
				}
				catch (Throwable e)
				{
					// :P
				}
			}
		}
		else
		{
			Matcher mod = replacementPattern.matcher( blk.modId );
			Matcher name = replacementPattern.matcher( blk.name );
			return get( mod.replaceAll( "" ), name.replaceAll( "" ), automatic ).getBoolean( automatic );
		}

		return false;
	}
}
