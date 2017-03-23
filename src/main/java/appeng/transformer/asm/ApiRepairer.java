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
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.Level;

import java.net.URL;
import java.net.URLConnection;


/*
 * It is a ClassTransformer which can transformer the older AE2 api class that some addons including,
 * which can occur the crash due to java.lang.NoSuchMethodError.
 * See also : https://github.com/xsun2001/Applied-Energistics-2-Unofficial/issues/1
 */
@Reflected
public class ApiRepairer implements IClassTransformer
{

	private LaunchClassLoader launchClassLoader;

	public ApiRepairer()
	{
		launchClassLoader = (LaunchClassLoader) this.getClass().getClassLoader();
		FMLRelaunchLog.log( "AE2-ApiRepairer", Level.INFO, "AE2 ApiFixer Installed" );
	}

	@Override public byte[] transform( String name, String transformedName, byte[] basicClass )
	{
		if( transformedName.startsWith( "appeng.api" ) )
		{
			try
			{
				String clazzurl = getClass().getResource( "" ).toString();
				clazzurl = clazzurl.substring( 0, clazzurl.length() - 23 ) + transformedName.replace( '.', '/' ) + ".class";
				//23 is "appeng/transformer/asm"'s length + 1
				URL url = new URL( clazzurl );
				URLConnection connection = url.openConnection();
				byte[] bytes = new byte[connection.getContentLength()];
				if( connection.getInputStream().read( bytes ) == -1 )
				{
					FMLRelaunchLog.log( "AE2-ApiRepairer", Level.ERROR, "Failed to fix api class [%s] because the new class couldn't be read", transformedName );
					return basicClass;
				}
				for( IClassTransformer ct : launchClassLoader.getTransformers() )
				{
					if( ct == this )
						continue;
					bytes = ct.transform( name, transformedName, bytes );
				}
				return bytes;
			}
			catch( Exception e )
			{
				FMLRelaunchLog.log( "AE2-ApiRepairer", Level.ERROR, "Failed to fix api class [%s] because of [%s]", transformedName, e.getClass().getName() );
				return basicClass;
			}
		}
		return basicClass;
	}
}
