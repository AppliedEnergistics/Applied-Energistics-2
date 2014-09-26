package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CraftType implements Comparable<CraftType>
{
	private static Map<Integer, Map<Integer, CraftType>> cache = new HashMap<Integer, Map<Integer, CraftType>>();
	private static Map<ArrayList, CraftType> arrayListCache = new HashMap<ArrayList, CraftType>();
	private int itemID, damage;
	private Item item;
	private Object stack;

	private CraftType(Item item, int itemDamage)
	{
		this.item = item;
		itemID = Item.getIdFromItem(item);
		damage = itemDamage;
		stack = new ItemStack(item, 1, damage);
	}

	private CraftType(ArrayList<ItemStack> items)
	{
		ItemStack itemStack = items.get(0);
		item = itemStack.getItem();
		itemID = Item.getIdFromItem(item);
		damage = CommonUtilities.getItemDamage(itemStack);
		stack = items;
	}

	public static CraftType getInstance(Object stack)
	{
		if(stack instanceof ItemStack)
		{
			return getInstance((ItemStack)stack);
		}
		else if(stack instanceof ArrayList && ((ArrayList)stack).size() > 0)
		{
			return getInstance((ArrayList)stack);
		}
		else
		{
			return null;
		}
	}

	private static CraftType getInstance(ArrayList stack)
	{
		CraftType type = arrayListCache.get(stack);

		if(type == null)
		{
			type = new CraftType(stack);
			arrayListCache.put(stack, type);
		}

		return type;
	}

	private static CraftType getInstance(ItemStack stack)
	{
		int id = Item.getIdFromItem(stack.getItem());
		Map<Integer, CraftType> map = cache.get(id);

		if(map == null)
		{
			map = new HashMap<Integer, CraftType>();
			cache.put(id, map);
		}

		CraftType type = map.get(CommonUtilities.getItemDamage(stack));

		if(type == null)
		{
			type = new CraftType(stack.getItem(), CommonUtilities.getItemDamage(stack));
			map.put(CommonUtilities.getItemDamage(stack), type);
		}

		return type;
	}

	public static boolean hasInstance(ItemStack stack)
	{
		int id = Item.getIdFromItem(stack.getItem());
		if(!cache.containsKey(id))
		{
			return false;
		}

		return cache.get(id).containsKey(CommonUtilities.getItemDamage(stack));
	}

	@Override
	public int compareTo(CraftType other)
	{
		if(this.itemID != other.itemID)
		{
			return this.itemID > other.itemID? 1 : -1;
		}
		else if(this.damage != other.damage)
		{
			return this.damage > other.damage? 1 : -1;
		}
		else if((this.stack instanceof ArrayList) != (other.stack instanceof ArrayList))
		{
			return (this.stack instanceof ArrayList)? -1 : 1;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && obj instanceof CraftType)
		{
			CraftType type = (CraftType)obj;

			if(stack instanceof ItemStack && type.stack instanceof ItemStack)
			{
				return type.itemID == this.itemID && type.damage == this.damage;
			}
			else if(stack instanceof ArrayList && type.stack instanceof ArrayList)
			{
				return stack.equals(type.stack);
			}
			else
			{
				return false;
			}
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return damage * 3571 + itemID;
	}

	public Object getStack()
	{
		return stack;
	}

	public ItemStack getDisplayStack()
	{
		if(stack instanceof ItemStack)
		{
			return (ItemStack)stack;
		}
		else if(stack instanceof ArrayList)
		{
			return (ItemStack)((ArrayList)stack).get(0);
		}
		else
		{
			return null;
		}
	}
}