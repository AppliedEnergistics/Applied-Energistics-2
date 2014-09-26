package uristqwerty.CraftGuide.api;

import java.util.ArrayList;
import java.util.List;

public class StackInfo
{
	public static List<StackInfoSource> sources = new ArrayList<StackInfoSource>();

	public static void addSource(StackInfoSource source)
	{
		sources.add(source);
	}
}
