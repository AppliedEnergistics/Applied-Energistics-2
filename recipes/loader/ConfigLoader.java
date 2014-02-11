package appeng.recipes.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import appeng.api.recipes.IRecipeLoader;

public class ConfigLoader implements IRecipeLoader
{

	private String rootPath;

	public ConfigLoader(String s) {
		rootPath = s;
	}

	@Override
	public BufferedReader getFile(String s) throws Exception
	{
		File f = new File( rootPath + s );
		return new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
	}
}
