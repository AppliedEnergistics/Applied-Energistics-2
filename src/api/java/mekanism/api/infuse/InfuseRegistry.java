package mekanism.api.infuse;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

/**
 * Use this class to add a new object that registers as an infuse object.
 * @author AidanBrady
 *
 */
public class InfuseRegistry
{
	/** The (private) map of ItemStacks and their related InfuseObjects. */
	private static Map<ItemStack, InfuseObject> infuseObjects = new HashMap<ItemStack, InfuseObject>();

	/** The (private) map of infuse names and their corresponding InfuseTypes. */
	private static Map<String, InfuseType> infuseTypes = new HashMap<String, InfuseType>();

	/**
	 * Registers an InfuseType into the registry. Call this in PreInit!
	 * @param infuse
	 */
	public static void registerInfuseType(InfuseType infuse)
	{
		if(infuseTypes.containsKey(infuse.name))
		{
			return;
		}

		infuseTypes.put(infuse.name, infuse);
	}

	/**
	 * Gets an InfuseType from it's name, or null if it doesn't exist.
	 * @param name - the name of the InfuseType to get
	 * @return the name's corresponding InfuseType
	 */
	public static InfuseType get(String name)
	{
		if(name.equals("null"))
		{
			return null;
		}

		return infuseTypes.get(name);
	}

	/**
	 * Whether or not the registry contains a correspondent InfuseType to a name.
	 * @param name - the name to check
	 * @return if the name has a coorespondent InfuseType
	 */
	public static boolean contains(String name)
	{
		return get(name) != null;
	}

	/**
	 * Registers a block or item that serves as an infuse object.  An infuse object will store a certain type and amount of infuse,
	 * and will deliver this amount to the Metallurgic Infuser's buffer of infuse.  The item's stack size will be decremented when
	 * it is placed in the Metallurgic Infuser's infuse slot, and the machine can accept the type and amount of infuse stored in the
	 * object.
	 * @param itemStack - stack the infuse object is linked to -- stack size is ignored
	 * @param infuseObject - the infuse object with the type and amount data
	 */
	public static void registerInfuseObject(ItemStack itemStack, InfuseObject infuseObject)
	{
		if(getObject(itemStack) != null)
		{
			return;
		}

		infuseObjects.put(itemStack, infuseObject);
	}

	/**
	 * Gets the InfuseObject data from an ItemStack.
	 * @param itemStack - the ItemStack to check
	 * @return the ItemStack's InfuseObject
	 */
	public static InfuseObject getObject(ItemStack itemStack)
	{
		for(Map.Entry<ItemStack, InfuseObject> obj : infuseObjects.entrySet())
		{
			if(itemStack.isItemEqual(obj.getKey()))
			{
				return obj.getValue();
			}
		}

		return null;
	}

	/**
	 * Gets the private map for InfuseObjects.
	 * @return private InfuseObject map
	 */
	public static final Map<ItemStack, InfuseObject> getObjectMap()
	{
		return infuseObjects;
	}

	/**
	 * Gets the private map for InfuseTypes.
	 * @return private InfuseType map
	 */
	public static final Map<String, InfuseType> getInfuseMap()
	{
		return infuseTypes;
	}
}
