package appeng.recipes.loader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import appeng.api.recipes.IRecipeLoader;

public class JarLoader implements IRecipeLoader
{

	private final String rootPath;

	public JarLoader(String s) {
		rootPath = s;
	}

	@Override
	public BufferedReader getFile(String s) throws Exception
	{
		return new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( rootPath + s ), "UTF-8" ) );
	}

}
