package appeng.integration.modules.jei;


import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;

import appeng.core.AppEng;


class InscriberRecipeCategory extends BlankRecipeCategory<InscriberRecipeWrapper>
{

	private static final int SLOT_INPUT_TOP = 0;
	private static final int SLOT_INPUT_MIDDLE = 1;
	private static final int SLOT_INPUT_BOTTOM = 2;
	private static final int SLOT_OUTPUT = 3;

	static final String UID = "appliedenergistics2.inscriber";

	private final IDrawable background;

	private final String localizedName;

	private final IDrawableAnimated progress;

	public InscriberRecipeCategory( IGuiHelper guiHelper )
	{
		ResourceLocation location = new ResourceLocation( AppEng.MOD_ID, "textures/guis/inscriber.png" );
		this.background = guiHelper.createDrawable( location, 44, 15, 97, 64 );
		this.localizedName = I18n.format( "tile.appliedenergistics2.inscriber.name" );

		IDrawableStatic progressDrawable = guiHelper.createDrawable( location, 135, 177, 6, 18, 24, 0, 91, 0 );
		this.progress = guiHelper.createAnimatedDrawable( progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM, false );
	}

	@Override
	public String getUid()
	{
		return UID;
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
	public void drawAnimations( Minecraft minecraft )
	{
		this.progress.draw( minecraft );
	}

	@Override
	public void setRecipe( IRecipeLayout recipeLayout, InscriberRecipeWrapper recipeWrapper, IIngredients ingredients )
	{
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init( SLOT_INPUT_TOP, true, 0, 0 );
		itemStacks.init( SLOT_INPUT_MIDDLE, true, 18, 23 );
		itemStacks.init( SLOT_INPUT_BOTTOM, true, 0, 46 );
		itemStacks.init( SLOT_OUTPUT, false, 68, 24 );

		itemStacks.set( ingredients );
	}
}
