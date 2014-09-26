package uristqwerty.CraftGuide;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import uristqwerty.CraftGuide.RecipeGeneratorImplementation.RecipeGeneratorForgeExtension;
import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.gui_craftguide.theme.ThemeManager;

public class CraftGuide
{
	public static CraftGuideSide side;
	public static CraftGuideLoaderSide loaderSide;

	public static ItemCraftGuide itemCraftGuide;
	private static Properties config = new Properties();

	private static Map<String, String> configComments;

	public static int resizeRate;
	public static int mouseWheelScrollRate;
	public static int defaultKeybind;
	public static boolean pauseWhileOpen = true;
	public static boolean gridPacking = true;
	public static boolean alwaysShowID = false;
	public static boolean textSearchRequiresShift = false;
	public static boolean enableKeybind = true;
	public static boolean newerBackgroundStyle = false;
	public static boolean hideMundanePotionRecipes = true;
	public static boolean insertBetterWithRenewablesRecipes = false;
	public static boolean enableItemRecipe;

	public static boolean betterWithRenewablesDetected = false;
	public static boolean needsRecipeRefresh = false;

	public static final int DAMAGE_WILDCARD = 32767;

	public void preInit(String iconName)
	{
		CraftGuideLog.init(new File(configDirectory(), "CraftGuide.log"));

		initForgeExtensions();
		loadProperties();

		side.preInit();

		ItemSlot.implementation = new ItemSlotImplementationImplementation();

		if(enableKeybind)
		{
			side.initKeybind();
		}

		addItem(iconName);
	}

	public void init()
	{
		try
		{
			Class.forName("uristqwerty.CraftGuide.recipes.DefaultRecipeProvider").newInstance();
			Class.forName("uristqwerty.CraftGuide.recipes.BrewingRecipes").newInstance();
		}
		catch(InstantiationException e)
		{
			CraftGuideLog.log(e);
		}
		catch(IllegalAccessException e)
		{
			CraftGuideLog.log(e);
		}
		catch(ClassNotFoundException e)
		{
			CraftGuideLog.log(e);
		}

		loadModRecipes("BTW", "uristqwerty.CraftGuide.recipes.BTWRecipes");
		addIC2Recipes();
		loadModRecipes("gregtech_addon", "uristqwerty.CraftGuide.recipes.GregTechRecipes");
		loadModRecipes("extendedWorkbench", "uristqwerty.CraftGuide.recipes.ExtendedWorkbench");
		loadModRecipes("BuildCraft|Factory", "uristqwerty.CraftGuide.recipes.BuildCraftRecipes");
		side.initNetworkChannels();
	}

	private void addIC2Recipes()
	{
		try
		{
			Class.forName("ic2.core.block.machine.tileentity.TileEntityMetalFormer");
			loadModRecipes("IC2", "uristqwerty.CraftGuide.recipes.IC2ExperimentalRecipes");
		}
		catch(ClassNotFoundException e)
		{
			loadModRecipes("IC2", "uristqwerty.CraftGuide.recipes.IC2Recipes");
		}
	}

	private void loadModRecipes(String modID, String recipeClass)
	{
		if(loaderSide.isModLoaded(modID))
		{
			try
			{
				Class.forName(recipeClass).newInstance();
			}
			catch(ClassNotFoundException e)
			{
				CraftGuideLog.log(e);
			}
			catch(Exception e)
			{
				CraftGuideLog.log(e, "", true);
			}
			catch(LinkageError e)
			{
				CraftGuideLog.log(e, "", true);
			}
		}
	}

	private void initForgeExtensions()
	{
		if(loaderSide.isModLoaded("Forge"))
		{
			try
			{
				RecipeGeneratorImplementation.forgeExt = (RecipeGeneratorForgeExtension)Class.forName("uristqwerty.CraftGuide.ForgeStuff").newInstance();
			}
			catch(InstantiationException e)
			{
				CraftGuideLog.log(e);
			}
			catch(IllegalAccessException e)
			{
				CraftGuideLog.log(e);
			}
			catch(ClassNotFoundException e)
			{
				CraftGuideLog.log(e);
			}
		}
	}

	private void addItem(String iconName)
	{
		itemCraftGuide = new ItemCraftGuide(iconName);
		loaderSide.addName(itemCraftGuide, "Crafting Guide");

		if(enableItemRecipe)
		{
			loaderSide.addRecipe(new ItemStack(itemCraftGuide), new Object[] {"pbp",
					"bcb", "pbp", Character.valueOf('c'), Blocks.crafting_table,
					Character.valueOf('p'), Items.paper, Character.valueOf('b'),
					Items.book});
		}
	}

	static
	{
		configComments = new HashMap<String, String>();
		configComments.put("newerBackgroundStyle", "If false, CraftGuide will use the images from CraftGuideRecipe.png for vanilla shaped crafting recipes, which is better for texture packs. If true, CraftGuide will use the default background (pieced together from parts of CraftGuide.png, then slot backgrounds drawn overtop), which is worse for texture packs, and looks identical without a texture pack.");
		configComments.put("hideMundanePotionRecipes", "Hides recipes that convert a useful potion into a mundane potion with the damage value 8192, which is basically a failed potion. In the vanilla ingredients, they only occur when you try to add an effect without first adding netherwart (or add a second effect ingredient without using netherwart in between). Note that 8192 means, to the vanilla brewing system 'can make a splash potion without losing it's effects', and a potion with a value of EXACTLY 8192 does not have any effects anyway.");
		configComments.put("logThemeDebugInfo", "If true, CraftGuide will output a lot of debugging text every time it reloads the themes.");
		configComments.put("gridPacking", "Affects whether CraftGuide distributes leftover horizontal space between columns, or puts it all at the far right. Currently not useful, as any grid with columns now resizes itself so that it doesn't have any leftover horizntal space that needs to be distributed.");
		configComments.put("resizeRate", "If greater than 0, the maximum number of pixels that the CraftGuide window will change size by each frame. When the effect was actually tried in-game, it just made the GUI feel slow, so defaults to 0 ('ALL the pixels!').");
		configComments.put("textSearchRequiresShift", "Normally, when typing in the item list search box, pressing enter instantly returns to the recipe list, using whatever you had typed as the text filter. If this option is true, you also have to hold shift, to avoid accidentally searching.");
		configComments.put("RecipeList_mouseWheelScrollRate", "How many rows to scroll for each unit of mouse wheel scrolliness.");
		configComments.put("enableItemRecipe", "Whether you can craft the CraftGuide item.");
		configComments.put("enableKeybind", "Whether CraftGuide sets up a keybind so that you can open it without the item.");
		configComments.put("PauseWhileOpen", "In singleplayer, whether the game is paused while you have CraftGuide open. If false, you can browse recipes while waiting for your machines to run, but it also means that a ninja creeper may be able to sneak up behind you while you are distracted.");
		configComments.put("alwaysShowID", "If true, item tooltips have an additional line showing their item ID and damage value. Added before the vanilla F3+H, it has a different format, and puts the item ID on a separate line from the item name. If this setting is false, CraftGuide will only show item IDs in this way in the rare case of an item error");
		configComments.put("defaultKeybind", "If Minecraft isn't properly loading changed keybinds, or you are putting together a config/modpack and want a different default value, you can change the default CraftGuide keybind here.");
	}

	private void setConfigDefaults()
	{
		config.setProperty("RecipeList_mouseWheelScrollRate", "3");
		config.setProperty("PauseWhileOpen", Boolean.toString(true));
		config.setProperty("resizeRate", "0");
		config.setProperty("gridPacking", Boolean.toString(true));
		config.setProperty("alwaysShowID", Boolean.toString(false));
		config.setProperty("textSearchRequiresShift", Boolean.toString(false));
		config.setProperty("enableKeybind", Boolean.toString(true));
		config.setProperty("enableItemRecipe", Boolean.toString(true));
		config.setProperty("newerBackgroundStyle", Boolean.toString(false));
		config.setProperty("hideMundanePotionRecipes", Boolean.toString(true));
		config.setProperty("insertBetterWithRenewablesRecipes", Boolean.toString(false));
		config.setProperty("logThemeDebugInfo", Boolean.toString(false));
		config.setProperty("defaultKeybind", Integer.toString(Keyboard.KEY_G));
	}

	/**
	 * Load configuration. If a configuration file exists in the new
	 * location, load from there. If not, but one exists in the old
	 * location, use that instead. If neither exists, just use the
	 * defaults.
	 *
	 * Afterwards, save it back to the new configuration directory
	 * (to create it if it doesn't exist, or to update it if it was
	 * created by an earlier version of CraftGuide that didn't have
	 * exactly the same set of properties).
	 */
	private void loadProperties()
	{
		File oldConfigDir = loaderSide.getConfigDir();
		File oldConfigFile = new File(oldConfigDir, "CraftGuide.cfg");
		File newConfigDir = configDirectory();
		File newConfigFile = newConfigDir == null? null : new File(newConfigDir, "CraftGuide.cfg");
		File configFile = null;

		if(newConfigFile != null && newConfigFile.exists())
		{
			configFile = newConfigFile;
		}
		else if(oldConfigFile.exists() && oldConfigFile.canRead())
		{
			configFile = oldConfigFile;
		}

		setConfigDefaults();

		if(configFile != null && configFile.exists() && configFile.canRead())
		{
			try
			{
				config.load(new FileInputStream(configFile));
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			resizeRate = Integer.valueOf(config.getProperty("resizeRate"));
		}
		catch(NumberFormatException e)
		{
		}

		try
		{
			mouseWheelScrollRate = Integer.valueOf(config
					.getProperty("RecipeList_mouseWheelScrollRate"));
		}
		catch(NumberFormatException e)
		{
		}

		try
		{
			defaultKeybind = Integer.valueOf(config.getProperty("defaultKeybind"));
		}
		catch(NumberFormatException e)
		{
		}

		pauseWhileOpen = Boolean.valueOf(config.getProperty("PauseWhileOpen"));
		gridPacking = Boolean.valueOf(config.getProperty("gridPacking"));
		alwaysShowID = Boolean.valueOf(config.getProperty("alwaysShowID"));
		textSearchRequiresShift = Boolean.valueOf(config.getProperty("textSearchRequiresShift"));
		enableKeybind = Boolean.valueOf(config.getProperty("enableKeybind"));
		newerBackgroundStyle = Boolean.valueOf(config.getProperty("newerBackgroundStyle"));
		hideMundanePotionRecipes = Boolean.valueOf(config.getProperty("hideMundanePotionRecipes"));
		insertBetterWithRenewablesRecipes = Boolean.valueOf(config.getProperty("insertBetterWithRenewablesRecipes"));
		ThemeManager.debugOutput = Boolean.valueOf(config.getProperty("logThemeDebugInfo"));
		enableItemRecipe = Boolean.valueOf(config.getProperty("enableItemRecipe"));

		if(newConfigFile != null && !newConfigFile.exists())
		{
			try
			{
				newConfigFile.createNewFile();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		if(newConfigFile != null && newConfigFile.exists() && newConfigFile.canWrite())
		{
			try
			{
				saveConfig(new FileOutputStream(newConfigFile));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	// config.store() does not permit per-setting comments.
	private void saveConfig(OutputStream outputStream) throws IOException
	{
		Set<String> properties = config.stringPropertyNames();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

		writer.write("# ");
		writer.write(new Date().toString());
		writer.newLine();

		for(String property: properties)
		{
			if(configComments.containsKey(property))
			{
				writer.newLine();
				writer.write("# ");
				writer.write(configComments.get(property));
				writer.newLine();
				writer.write(property);
				writer.write('=');
				writer.write(config.getProperty(property));
				writer.newLine();
			}
		}

		writer.newLine();

		for(String property: properties)
		{
			if(!configComments.containsKey(property))
			{
				writer.write(property);
				writer.write('=');
				writer.write(config.getProperty(property));
				writer.newLine();
			}
		}

		writer.close();
	}

	public static File configDirectory()
	{
		File dir = new File(loaderSide.getConfigDir(), "CraftGuide");

		if(!dir.exists() && !dir.mkdirs())
		{
			return null;
		}

		return dir;
	}

	public static String getTranslation(String string)
	{
		if(string.equals("filter_type.input"))
		{
			return "Input";
		}
		else if(string.equals("filter_type.output"))
		{
			return "Output";
		}
		else if(string.equals("filter_type.machine"))
		{
			return "Machine";
		}
		else if(string.equals("filter"))
		{
			return "Filter";
		}
		else
		{
			return null;
		}
	}
}
