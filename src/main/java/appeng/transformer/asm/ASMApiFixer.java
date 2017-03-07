/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.transformer.asm;


import appeng.helpers.Reflected;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/*
 * It is a ClassTransformer which can transformer the older AE2 api class that some addons including,
 * which can occur the crash due to java.lang.NoSuchMethodError.
 * See also : https://github.com/xsun2001/Applied-Energistics-2-Unofficial/issues/1
 */
@Reflected
public class ASMApiFixer implements IClassTransformer
{

	public ASMApiFixer()
	{
		FMLRelaunchLog.log( "AE2-ApiFixer", Level.INFO, "ApiFixer Installed" );
		FMLRelaunchLog.log( "AE2-ApiFixer", Level.INFO, "%s", getClass().getResource( "" ).toString() );
	}

	@Override public byte[] transform( String name, String transformedName, byte[] basicClass )
	{
		if( transformedName.startsWith( "appeng.api" ) )
		{
			FMLRelaunchLog.log( "AE2-ApiFixer", Level.INFO, "Fixing api class file:%s", transformedName );
			try
			{
				String clazzurl = getClass().getResource( "" ).toString();
				clazzurl = clazzurl.substring( 0, clazzurl.length() - 23 ) + transformedName.replace( '.', '/' ) + ".class";
				//23 is "appeng/transformer/asm"'s length + 1
				FMLRelaunchLog.log( "AE2-ApiFixer", Level.INFO, "Fixing...:%s", clazzurl );
				URL url = new URL( clazzurl );
				URLConnection connection = url.openConnection();
				byte[] bytes = new byte[connection.getContentLength()];
				if( connection.getInputStream().read( bytes ) == -1 )
				{
					FMLRelaunchLog.log( "AE2-ApiFixer", Level.ERROR, "Cannot fix:%s", transformedName );
					return basicClass;
				}
				else
				{
					FMLRelaunchLog.log( "AE2-ApiFixer", Level.INFO, "Fix success:%s", transformedName );
					return bytes;
				}
			}
			catch( Exception e )
			{
				FMLRelaunchLog.log( "AE2-ApiFixer", Level.ERROR, "Fix failed:%s|%s", transformedName, e.getClass().getName() );
				return basicClass;
			}
		}
		return basicClass;
	}
}
