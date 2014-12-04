package appeng.api.recipes;

import java.io.BufferedReader;

public interface IRecipeLoader
{

	BufferedReader getFile(String s) throws Exception;

}
