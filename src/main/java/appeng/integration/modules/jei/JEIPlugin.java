/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.modules.jei;


import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.recipes.handlers.GrinderRecipe;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;


@JeiPlugin
public class JEIPlugin implements IModPlugin
{
	private static final ResourceLocation ID = new ResourceLocation(AppEng.MOD_ID, "core");

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerItemSubtypes( ISubtypeRegistration subtypeRegistry )
	{
		final Optional<Item> maybeFacade = AEApi.instance().definitions().items().facade().maybeItem();
		maybeFacade.ifPresent( subtypeRegistry::useNbtForSubtypes );
	}

	@Override
	public void registerCategories( IRecipeCategoryRegistration registry )
	{
		registry.addRecipeCategories( new GrinderRecipeCategory( registry.getJeiHelpers().getGuiHelper() ),
		 new CondenserCategory( registry.getJeiHelpers().getGuiHelper() ),
		 new InscriberRecipeCategory( registry.getJeiHelpers().getGuiHelper() ) );
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		// Allow recipe transfer from JEI to crafting and pattern terminal
		registration
				.addRecipeTransferHandler( new RecipeTransferHandler<>( ContainerCraftingTerm.class ),
						VanillaRecipeCategoryUid.CRAFTING );
		registration
				.addRecipeTransferHandler( new RecipeTransferHandler<>( ContainerPatternTerm.class ),
						VanillaRecipeCategoryUid.CRAFTING );
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {

		IDefinitions definitions = AEApi.instance().definitions();

//	FIXME	 Optional<Item> itemFacade = definitions.items().facade().maybeItem();
//	FIXME	 Optional<ItemStack> cableAnchor = definitions.parts().cableAnchor().maybeStack( 1 );
//	FIXME	 if( itemFacade.isPresent() && cableAnchor.isPresent() && AEConfig.instance().isFeatureEnabled( AEFeature.ENABLE_FACADE_CRAFTING ) )
//	FIXME	 {
//	FIXME	 	registration.addRecipeRegistryPlugin( new FacadeRegistryPlugin( (ItemFacade) itemFacade.get(), cableAnchor.get() ) );
//	FIXME	 }

		ItemStack condenser = definitions.blocks().condenser().maybeStack( 1 ).orElse( ItemStack.EMPTY );
		if(!condenser.isEmpty()) {
			ItemStack matterBall = definitions.materials().matterBall().maybeStack(1).orElse(ItemStack.EMPTY);
			if (!matterBall.isEmpty()) {
				registration.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS), CondenserCategory.UID);
			}
			ItemStack singularity = definitions.materials().singularity().maybeStack(1).orElse(ItemStack.EMPTY);
			if (!singularity.isEmpty()) {
				registration.addRecipes(ImmutableList.of(CondenserOutput.SINGULARITY), CondenserCategory.UID);
			}
		}

		RecipeManager recipeManager = Minecraft.getInstance().world.getRecipeManager();
		registration.addRecipes(recipeManager.getRecipes(GrinderRecipe.TYPE).values(), GrinderRecipeCategory.UID);
		// FIXME registration.addRecipes( recipeManager.getRecipes(InscriberRecipe.TYPE), InscriberRecipeCategory.UID );
		registration.addRecipes(ImmutableList.of(CondenserOutput.MATTER_BALLS, CondenserOutput.SINGULARITY), CondenserCategory.UID);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		IDefinitions definitions = AEApi.instance().definitions();

		ItemStack grindstone = definitions.blocks().grindstone().maybeStack( 1 ).orElse( ItemStack.EMPTY );
		if( !grindstone.isEmpty() )
		{
			registration.addRecipeCatalyst( grindstone, GrinderRecipeCategory.UID );
		}

		definitions.blocks().condenser().maybeStack( 1 ).ifPresent(condenser -> {
			registration.addRecipeCatalyst(condenser, CondenserCategory.UID);
		});

		// Register the inscriber as the crafting item for the inscription category
		definitions.blocks().inscriber().maybeStack( 1 ).ifPresent( inscriber ->
		{
			registration.addRecipeCatalyst( inscriber, InscriberRecipeCategory.UID );
		} );

	}

	private void registerDescriptions(IDefinitions definitions, IRecipeRegistration registry )
	{
		IMaterials materials = definitions.materials();

		final String message;
		if( AEConfig.instance().isFeatureEnabled( AEFeature.CERTUS_QUARTZ_WORLD_GEN ) )
		{
			message = GuiText.ChargedQuartz.getLocal() + "\n\n" + GuiText.ChargedQuartzFind.getLocal();
		}
		else
		{
			message = GuiText.ChargedQuartzFind.getLocal();
		}
		this.addDescription( registry, materials.certusQuartzCrystalCharged(), message );

		if( AEConfig.instance().isFeatureEnabled( AEFeature.METEORITE_WORLD_GEN ) )
		{
			this.addDescription( registry, materials.logicProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
			this.addDescription( registry, materials.calcProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
			this.addDescription( registry, materials.engProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.IN_WORLD_FLUIX ) )
		{
			this.addDescription( registry, materials.fluixCrystal(), GuiText.inWorldFluix.getLocal() );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.IN_WORLD_SINGULARITY ) )
		{
			this.addDescription( registry, materials.qESingularity(), GuiText.inWorldSingularity.getLocal() );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.IN_WORLD_PURIFICATION ) )
		{
			this.addDescription( registry, materials.purifiedCertusQuartzCrystal(), GuiText.inWorldPurificationCertus.getLocal() );
			this.addDescription( registry, materials.purifiedNetherQuartzCrystal(), GuiText.inWorldPurificationNether.getLocal() );
			this.addDescription( registry, materials.purifiedFluixCrystal(), GuiText.inWorldPurificationFluix.getLocal() );
		}

	}

	private void addDescription(IRecipeRegistration registry, IItemDefinition itemDefinition, String message )
	{
		itemDefinition.maybeStack( 1 ).ifPresent( itemStack -> registry.addIngredientInfo( itemStack, VanillaTypes.ITEM, message ) );
	}

	@Override
	public void onRuntimeAvailable( IJeiRuntime jeiRuntime )
	{
// FIXME JEIModule jeiModule = (JEIModule) Integrations.jei();
// FIXME jeiModule.setJei( new JeiRuntimeAdapter( jeiRuntime ) );
	}
}
