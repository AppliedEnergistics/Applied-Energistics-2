package appeng.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.lang3.StringUtils;

import appeng.core.AELog;

public class Profiler implements Runnable
{

	public static Profiler instance = null;
	public boolean profile = false;
	public boolean isRunning = true;
	public final Object lock = new Object();

	public static String getCommandOutput(String command)
	{
		String output = null; // the string to return

		Process process = null;
		BufferedReader reader = null;
		InputStreamReader streamReader = null;
		InputStream stream = null;

		try
		{
			process = Runtime.getRuntime().exec( command );

			stream = process.getInputStream();
			streamReader = new InputStreamReader( stream );
			reader = new BufferedReader( streamReader );

			String currentLine = null;
			StringBuilder commandOutput = new StringBuilder();
			while ((currentLine = reader.readLine()) != null)
			{
				commandOutput.append( currentLine );
			}

			int returnCode = process.waitFor();
			if ( returnCode == 0 )
			{
				output = commandOutput.toString();
			}

		}
		catch (IOException e)
		{
			output = null;
		}
		catch (InterruptedException e)
		{
		}
		finally
		{

			if ( stream != null )
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
				}
			}
			if ( streamReader != null )
			{
				try
				{
					streamReader.close();
				}
				catch (IOException e)
				{
				}
			}
			if ( reader != null )
			{
				try
				{
					streamReader.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		return output;
	}

	private String findJDK()
	{
		if ( System.getProperty( "os.name" ).contains( "win" ) || System.getProperty( "os.name" ).contains( "Win" ) )
		{
			String path = getCommandOutput( "where javac" );
			if ( path == null || path.isEmpty() )
			{

			}
			else
			{
				File javacFile = new File( path );
				File jdkInstallationDir = javacFile.getParentFile().getParentFile();
				return jdkInstallationDir.getPath();
			}
		}
		else
		{
			String response = getCommandOutput( "whereis javac" );
			if ( response == null )
			{

			}
			else
			{
				int pathStartIndex = response.indexOf( '/' );
				if ( pathStartIndex == -1 )
				{
				}
				else
				{
					String path = response.substring( pathStartIndex, response.length() );
					File javacFile = new File( path );
					File jdkInstallationDir = javacFile.getParentFile().getParentFile();
					return jdkInstallationDir.getPath();
				}
			}
		}
		return null;
	}

	Class ProfilingSettingsPresets;
	Class CPUResultsSnapshot;
	Class StackTraceSnapshotBuilder;
	Class LoadedSnapshot;
	Class Lookup;
	Class LookupProvider;
	Class ResultsSnapshot;
	Class ProfilingSettings;

	Method addStacktrace;
	Method createSnapshot;
	Method createCPUPreset;
	Method save;
	Constructor LoadedSnapshot_Constructor;

	@Override
	public void run()
	{
		instance = this;

		try
		{
			String JDKpath = findJDK();

			String root = StringUtils.join( new String[] { JDKpath, "lib", "visualvm" }, File.separator );
			String Base = StringUtils.join( new String[] { root, "profiler", "modules", "" }, File.separator );
			String BaseB = StringUtils.join( new String[] { root, "platform", "lib", "" }, File.separator );

			File a = new File( Base + "org-netbeans-lib-profiler.jar" );
			File b = new File( Base + "org-netbeans-lib-profiler-common.jar" );
			File c = new File( Base + "org-netbeans-modules-profiler.jar" );
			File d = new File( BaseB + "org-openide-util.jar" );
			File e = new File( BaseB + "org-openide-util-lookup.jar" );

			ClassLoader cl = URLClassLoader.newInstance( new URL[] { a.toURI().toURL(), b.toURI().toURL(), c.toURI().toURL(), d.toURI().toURL(),
					e.toURI().toURL() } );

			ProfilingSettingsPresets = cl.loadClass( "org.netbeans.lib.profiler.common.ProfilingSettingsPresets" );
			ProfilingSettings = cl.loadClass( "org.netbeans.lib.profiler.common.ProfilingSettings" );
			CPUResultsSnapshot = cl.loadClass( "org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot" );
			ResultsSnapshot = cl.loadClass( "org.netbeans.lib.profiler.results.ResultsSnapshot" );
			StackTraceSnapshotBuilder = cl.loadClass( "org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder" );
			LoadedSnapshot = cl.loadClass( "org.netbeans.modules.profiler.LoadedSnapshot" );
			Lookup = cl.loadClass( "org.openide.util.Lookup" );

			for (Class dc : Lookup.getDeclaredClasses())
			{
				if ( dc.getSimpleName().equals( "Provider" ) )
					LookupProvider = dc;
			}

			if ( LookupProvider == null )
				throw new ClassNotFoundException( "Lookup.Provider" );

			addStacktrace = StackTraceSnapshotBuilder.getMethod( "addStacktrace", ThreadInfo[].class, long.class );
			createSnapshot = StackTraceSnapshotBuilder.getMethod( "createSnapshot", long.class );
			createCPUPreset = ProfilingSettingsPresets.getMethod( "createCPUPreset" );
			save = LoadedSnapshot.getMethod( "save", DataOutputStream.class );
			LoadedSnapshot_Constructor = LoadedSnapshot.getConstructor( ResultsSnapshot, ProfilingSettings, File.class, LookupProvider );
		}
		catch (Throwable t)
		{
			isRunning = false;
			AELog.info( "Unable to find/load JDK, profiling disabled." );
			return;
		}

		int limit = 0;
		while (isRunning)
		{
			if ( profile )
			{
				limit = 9000;

				try
				{
					Object StackTraceSnapshotBuilder_Instance = StackTraceSnapshotBuilder.newInstance();
					ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

					while (profile)
					{
						if ( limit-- < 0 )
							profile = false;

						for (long th : mxBean.getAllThreadIds())
						{
							ThreadInfo ti = mxBean.getThreadInfo( th, Integer.MAX_VALUE );
							addStacktrace.invoke( StackTraceSnapshotBuilder_Instance, new ThreadInfo[] { ti }, System.nanoTime() );
						}

						Thread.sleep( 20 );
					}

					Object CPUResultsSnapshot_Instance = createSnapshot.invoke( StackTraceSnapshotBuilder_Instance, System.currentTimeMillis() );

					Object LoadedSnapshot_Instance = LoadedSnapshot_Constructor.newInstance( CPUResultsSnapshot_Instance,
							createCPUPreset.invoke( ProfilingSettingsPresets ), null, null );

					FileOutputStream bout = new FileOutputStream( new File( "ae-latest-profile.nps" ) );
					DataOutputStream out = new DataOutputStream( bout );
					save.invoke( LoadedSnapshot_Instance, out );
					out.flush();
					bout.close();
				}
				catch (Throwable t)
				{
					AELog.severe( "Error while profiling" );
					t.printStackTrace();
					profile = false;
					return;
				}
			}
			else
			{
				try
				{
					synchronized (lock)
					{
						lock.wait();
					}
				}
				catch (InterruptedException e)
				{

				}
			}

		}

	}
}