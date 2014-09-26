package uristqwerty.CraftGuide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.item.ItemStack;
import uristqwerty.CraftGuide.api.BasicRecipeFilter;
import uristqwerty.CraftGuide.api.CraftGuideRecipe;
import uristqwerty.CraftGuide.api.CraftGuideRecipeExtra1;
import uristqwerty.CraftGuide.api.ItemFilter;
import uristqwerty.CraftGuide.api.RecipeFilter;
import uristqwerty.CraftGuide.api.RecipeProvider;
import uristqwerty.CraftGuide.api.SlotType;
import uristqwerty.CraftGuide.client.ui.IRecipeCacheListener;

public class RecipeCache
{
	private SortedSet<CraftType> craftingTypes = new TreeSet<CraftType>();
	private Map<CraftType, List<CraftGuideRecipe>> craftResults = new HashMap<CraftType, List<CraftGuideRecipe>>();
	private List<CraftGuideRecipe> typeResults;
	private List<CraftGuideRecipe> filteredResults;
	private RecipeGeneratorImplementation generator = new RecipeGeneratorImplementation();
	private ItemFilter filterItem = null;
	private List<IRecipeCacheListener> listeners = new LinkedList<IRecipeCacheListener>();
	private Set<CraftType> currentTypes = null;
	private SortedSet<CraftType> allItems = new TreeSet<CraftType>();
	private boolean firstReset = true;

	public RecipeCache()
	{
		reset();
	}

	public void reset()
	{
		CraftGuide.needsRecipeRefresh = false;

		CraftGuideLog.log("(re)loading recipe list...");
		Map<ItemStack, List<CraftGuideRecipe>> rawRecipes = generateRecipes();

		filterRawRecipes(rawRecipes);
		craftResults.clear();

		for(ItemStack key: rawRecipes.keySet())
		{
			CraftType type = CraftType.getInstance(key);

			if(type == null)
			{
				CraftGuideLog.log("  Error: null type, " + rawRecipes.get(key).size() + " recipes skipped");
				continue;
			}

			if(!craftResults.containsKey(type))
			{
				craftResults.put(type, new ArrayList<CraftGuideRecipe>());
			}

			craftResults.get(type).addAll(rawRecipes.get(key));
		}

		generator.clearRecipes();

		generateAllItemList();

		craftingTypes.addAll(craftResults.keySet());

		if(firstReset)
		{
			currentTypes = new HashSet<CraftType>();
			currentTypes.addAll(craftingTypes);

			for(ItemStack stack: generator.disabledTypes)
			{
				currentTypes.remove(CraftType.getInstance(stack));
			}

			firstReset = false;
		}

		setTypes(currentTypes);

		for(IRecipeCacheListener listener: listeners)
		{
			listener.onReset(this);
		}
	}

	private void generateAllItemList()
	{
		allItems.clear();

		for(List<CraftGuideRecipe> type: craftResults.values())
		{
			for(CraftGuideRecipe recipe: type)
			{
				for(Object item: recipe.getItems())
				{
					if(item != null)
					{
						if(item instanceof ItemStack)
						{
							allItems.add(CraftType.getInstance(item));
						}
						else if(item instanceof ArrayList)
						{
							for(ItemStack stack: (ArrayList<ItemStack>)item)
							{
								CraftType craftType = CraftType.getInstance(stack);

								if(craftType != null)
								{
									allItems.add(craftType);
								}
							}

							CraftType craftType = CraftType.getInstance(item);

							if(craftType != null)
							{
								allItems.add(craftType);
							}
						}
					}
				}
			}
		}

		removeUselessDuplicates();
	}

	private void removeUselessDuplicates()
	{
		Map<CraftType, Integer> wild = new HashMap<CraftType, Integer>();
		for(CraftType item: allItems)
		{
			if(!(item.getStack() instanceof ItemStack))
			{
				continue;
			}

			if(CommonUtilities.getItemDamage(((ItemStack)item.getStack())) == CraftGuide.DAMAGE_WILDCARD)
			{
				wild.put(item, 1);
			}
			else
			{
				for(CraftType type: wild.keySet())
				{
					if(((ItemStack)item.getStack()).getItem() == ((ItemStack)type.getStack()).getItem())
					{
						wild.put(type, wild.get(type) + 1);
					}
				}
			}
		}

		for(CraftType type: wild.keySet())
		{
			if(wild.get(type) == 2)
			{
				allItems.remove(type);
			}
			else if(wild.get(type) == 1)
			{
				allItems.remove(type);
				allItems.add(CraftType.getInstance(new ItemStack(type.getDisplayStack().getItem(), 1, 0)));
			}
		}

		wild.clear();
		for(CraftType item: allItems)
		{
			if(item.getStack() instanceof ArrayList &&
					((ArrayList)item.getStack()).size() == 1 &&
					((ArrayList)item.getStack()).get(0) != null)
			{
				wild.put(item, 1);
				ItemStack stack = (ItemStack)((ArrayList)item.getStack()).get(0);

				for(CraftType item2: allItems)
				{
					if(item2.getStack() instanceof ItemStack)
					{
						if(CommonUtilities.checkItemStackMatch(stack, (ItemStack)item2.getStack()))
						{
							wild.put(item, 2);
							break;
						}
					}
				}
			}
		}

		for(CraftType type: wild.keySet())
		{
			if(wild.get(type) == 2)
			{
				allItems.remove(type);
			}
		}
	}

	private Map<ItemStack, List<CraftGuideRecipe>> generateRecipes()
	{
		generator.clearRecipes();
		CraftGuideLog.log("  Getting recipes...");
		for(Object object: ReflectionAPI.APIObjects)
		{
			if(object instanceof RecipeProvider)
			{
				CraftGuideLog.log("    Generating recipes from " + object.getClass().getName());
				try
				{
					((RecipeProvider)object).generateRecipes(generator);
				}
				catch(Exception e)
				{
					CraftGuideLog.log(e);
				}
				catch(LinkageError e)
				{
					CraftGuideLog.log(e);
				}
			}
		}

		return generator.getRecipes();
	}

	private void filterRawRecipes(Map<ItemStack, List<CraftGuideRecipe>> rawRecipes)
	{
		CraftGuideLog.log("  Filtering recipes...");
		for(Object object: ReflectionAPI.APIObjects)
		{
			if(object instanceof RecipeFilter)
			{
				CraftGuideLog.log("    Filtering recipes from " + object.getClass().getName());

				try
				{
					for(ItemStack type: rawRecipes.keySet())
					{
						rawRecipes.put(type, ((RecipeFilter)object).filterRecipes(rawRecipes.get(type), type));
					}
				}
				catch(Exception e)
				{
					CraftGuideLog.log(e);
				}
				catch(LinkageError e)
				{
					CraftGuideLog.log(e);
				}
			}
			else if(object instanceof BasicRecipeFilter)
			{

				CraftGuideLog.log("    Filtering recipes from " + object.getClass().getName());

				try
				{
					for(ItemStack type: rawRecipes.keySet())
					{
						Iterator<CraftGuideRecipe> iterator = rawRecipes.get(type).iterator();

						while(iterator.hasNext())
						{
							if(!((BasicRecipeFilter)object).shouldKeepRecipe(iterator.next(), type))
							{
								iterator.remove();
							}
						}
					}
				}
				catch(Exception e)
				{
					CraftGuideLog.log(e);
				}
				catch(LinkageError e)
				{
					CraftGuideLog.log(e);
				}
			}
		}
	}

	public void setTypes(Set<CraftType> types)
	{
		typeResults = new ArrayList<CraftGuideRecipe>();
		currentTypes = types;

		if(types == null)
		{
			for(CraftType type: craftingTypes)
			{
				typeResults.addAll(craftResults.get(type));
			}
		}
		else
		{
			for(CraftType type: craftingTypes)
			{
				if(types.contains(type))
				{
					typeResults.addAll(craftResults.get(type));
				}
			}
		}

		filter(filterItem);
	}

	public List<CraftGuideRecipe> getRecipes()
	{
		return filteredResults;
	}

	public void filter(ItemFilter filter)
	{
		filterItem = filter;

		boolean input = GuiCraftGuide.filterSlotTypes.get(SlotType.INPUT_SLOT);
		boolean output = GuiCraftGuide.filterSlotTypes.get(SlotType.OUTPUT_SLOT);
		boolean machine = GuiCraftGuide.filterSlotTypes.get(SlotType.MACHINE_SLOT);

		if(filter == null)
		{
			filteredResults = typeResults;
		}
		else
		{
			filteredResults = new ArrayList<CraftGuideRecipe>();

			for(CraftGuideRecipe recipe: typeResults)
			{
				if(recipe instanceof CraftGuideRecipeExtra1)
				{
					CraftGuideRecipeExtra1 e = (CraftGuideRecipeExtra1)recipe;

					if((input && e.containsItem(filter, SlotType.INPUT_SLOT))
					|| (output && e.containsItem(filter, SlotType.OUTPUT_SLOT))
					|| (machine && e.containsItem(filter, SlotType.MACHINE_SLOT)))
					{
						filteredResults.add(recipe);
					}
				}
				else if(recipe.containsItem(filter))
				{
					filteredResults.add(recipe);
				}
			}
		}

		for(IRecipeCacheListener listener: listeners)
		{
			listener.onChange(this);
		}
	}

	public ItemFilter getFilter()
	{
		return filterItem;
	}

	public Set<CraftType> getCraftTypes()
	{
		return craftingTypes;
	}

	public void addListener(IRecipeCacheListener listener)
	{
		listeners.add(listener);
	}

	public SortedSet<CraftType> getAllItems()
	{
		return allItems;
	}

	public Set<CraftType> getFilterTypes()
	{
		return currentTypes;
	}
}
