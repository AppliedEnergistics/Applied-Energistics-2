package appeng.transformer;

import net.minecraft.launchwrapper.IClassTransformer;

public class AppEngATTransformer implements IClassTransformer
{

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		return basicClass;
	}

}
