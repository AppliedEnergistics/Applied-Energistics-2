package appeng.api.recipes;

import net.minecraft.nbt.NBTTagCompound;

public class ResolverResult
{

	final public String itemName;
	final public int damageValue;
	final public NBTTagCompound compound;

	public ResolverResult(String name, int damage) {
		itemName = name;
		damageValue = damage;
		compound = null;
	}

	public ResolverResult(String name, int damage, NBTTagCompound data) {
		itemName = name;
		damageValue = damage;
		compound = data;
	}

}
