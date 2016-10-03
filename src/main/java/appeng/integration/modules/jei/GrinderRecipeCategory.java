package appeng.integration.modules.jei;


import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;

import appeng.core.AppEng;


class GrinderRecipeCategory extends BlankRecipeCategory<GrinderRecipeWrapper>
{

	public static final String UID = "appliedenergistics2.grinder";

	private final String localizedName;

	private final IDrawable background;

	public GrinderRecipeCategory( IGuiHelper guiHelper )
	{
		this.localizedName = I18n.format( "tile.appliedenergistics2.grindstone.name" );

		ResourceLocation location = new ResourceLocation( AppEng.MOD_ID, "textures/guis/grinder.png" );
		this.background = guiHelper.createDrawable( location, 11, 16, 154, 64 );
	}

	@Override
	public String getUid()
	{
		return GrinderRecipeCategory.UID;
	}

	@Override
	public String getTitle()
	{
		return localizedName;
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public void setRecipe( IRecipeLayout recipeLayout, GrinderRecipeWrapper recipeWrapper, IIngredients ingredients )
	{
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init( 0, true, 0, 0 );
		itemStacks.init( 1, false, 100, 46 );
		itemStacks.init( 2, false, 118, 46 );
		itemStacks.init( 3, false, 136, 46 );

		itemStacks.set( ingredients );
	}
}
