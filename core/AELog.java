package appeng.core;

import java.io.File;
import java.util.logging.Level;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

public class AELog
{

	public static cpw.mods.fml.relauncher.FMLRelaunchLog instance = cpw.mods.fml.relauncher.FMLRelaunchLog.log;
	public static net.minecraftforge.common.Configuration localizeation = new net.minecraftforge.common.Configuration( new File( "en_us.lang" ) );

	private AELog() {
	}

	private static void log(Level level, String format, Object... data)
	{
		FMLRelaunchLog.log( "AE", level, format, data );
	}

	public static void severe(String format, Object... data)
	{
		log( Level.SEVERE, format, data );
	}

	public static void warning(String format, Object... data)
	{
		log( Level.WARNING, format, data );
	}

	public static void info(String format, Object... data)
	{
		log( Level.INFO, format, data );
	}

	public static void grinder(String o)
	{
		log( Level.FINEST, "grinder: " + o );
	}

	public static void localization(String category, String unlocalizedName)
	{
		localizeation.get( category, unlocalizedName, unlocalizedName );
		localizeation.save();
	}

	public static void error(Throwable e)
	{
		log( Level.SEVERE, "Error Occurred" );
		e.printStackTrace();
	}

}
