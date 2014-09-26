package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.List;

public class ReflectionAPI
{
	public static List<Object> APIObjects = new ArrayList<Object>();

	public static void registerAPIObject(Object object)
	{
		APIObjects.add(object);
	}

	public static void reloadRecipes()
	{
		CraftGuide.side.reloadRecipes();
	}
}
