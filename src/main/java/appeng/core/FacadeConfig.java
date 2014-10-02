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
	final Pattern replacementPattern;

	public FacadeConfig(String path) {
		super( new File( path + "Facades.cfg" ) );
		replacementPattern = Pattern.compile( "[^a-zA-Z0-9]" );
	}

	public boolean checkEnabled(Block id, int metadata, boolean automatic)
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
						return get( "minecraft", f.getName() + (metadata == 0 ? "" : "." + metadata), automatic ).getBoolean( automatic );
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
			return get( mod.replaceAll( "" ), name.replaceAll( "" ) + (metadata == 0 ? "" : "." + metadata), automatic ).getBoolean( automatic );
		}

		return false;
	}
}
