package appeng.integration.modules.jei;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;

import appeng.api.features.IGrinderEntry;


class GrinderRecipeWrapper extends BlankRecipeWrapper
{

	private final IGrinderEntry recipe;

	GrinderRecipeWrapper( IGrinderEntry recipe )
	{
		this.recipe = recipe;
	}

	@Override
	public void getIngredients( IIngredients ingredients )
	{
		ingredients.setInput( ItemStack.class, recipe.getInput() );
		List<ItemStack> outputs = new ArrayList<>( 3 );
		outputs.add( recipe.getOutput() );
		if( recipe.getOptionalOutput() != null )
		{
			outputs.add( recipe.getOptionalOutput() );
		}
		if( recipe.getSecondOptionalOutput() != null )
		{
			outputs.add( recipe.getSecondOptionalOutput() );
		}
		ingredients.setOutputs( ItemStack.class, outputs );
	}

	@Override
	public void drawInfo( Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY )
	{

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		int x = 118;

		final float scale = 0.85f;
		final float invScale = 1 / scale;
		GlStateManager.scale( scale, scale, 1 );

		if( recipe.getOptionalOutput() != null )
		{
			String text = String.format( "%d%%", (int) ( recipe.getOptionalChance() * 100 ) );
			float width = fr.getStringWidth( text ) * scale;
			int xScaled = (int) Math.round( ( x + ( 18 - width ) / 2 ) * invScale );
			fr.drawString( text, xScaled, (int) ( 65 * invScale ), Color.gray.getRGB() );
			x += 18;
		}

		if( recipe.getSecondOptionalOutput() != null )
		{
			String text = String.format( "%d%%", (int) ( recipe.getSecondOptionalChance() * 100 ) );
			float width = fr.getStringWidth( text ) * scale;
			int xScaled = (int) Math.round( ( x + ( 18 - width ) / 2 ) * invScale );
			fr.drawString( text, xScaled, (int) ( 65 * invScale ), Color.gray.getRGB() );
		}

		GlStateManager.scale( invScale, invScale, 1 );
	}
}
