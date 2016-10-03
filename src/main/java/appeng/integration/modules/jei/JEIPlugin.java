package appeng.integration.modules.jei;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;

import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.IInscriberRecipe;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.modules.JEI;
import appeng.items.parts.ItemFacade;


@mezz.jei.api.JEIPlugin
public class JEIPlugin extends BlankModPlugin
{

	@Override
	public void register( IModRegistry registry )
	{
		registry.addRecipeHandlers( new ShapedRecipeHandler(), new ShapelessRecipeHandler() );

		IDefinitions definitions = AEApi.instance().definitions();

		registerFacadeRecipe( definitions, registry );

		registerInscriberRecipes( definitions, registry );

		registerCondenserRecipes( definitions, registry );

		registerGrinderRecipes( definitions, registry );

		registerDescriptions( definitions, registry );

		// Allow recipe transfer from JEI to crafting and pattern terminal
		registry.getRecipeTransferRegistry().addRecipeTransferHandler( new RecipeTransferHandler<>( ContainerCraftingTerm.class ) );
		registry.getRecipeTransferRegistry().addRecipeTransferHandler( new RecipeTransferHandler<>( ContainerPatternTerm.class ) );
	}

	private void registerDescriptions( IDefinitions definitions, IModRegistry registry )
	{
		IMaterials materials = definitions.materials();

		final String message;
		if( AEConfig.instance.isFeatureEnabled( AEFeature.CertusQuartzWorldGen ) )
		{
			message = GuiText.ChargedQuartz.getLocal() + "\n\n" + GuiText.ChargedQuartzFind.getLocal();
		}
		else
		{
			message = GuiText.ChargedQuartzFind.getLocal();
		}
		addDescription( registry, materials.certusQuartzCrystalCharged(), message );

		if( AEConfig.instance.isFeatureEnabled( AEFeature.MeteoriteWorldGen ) )
		{
			addDescription( registry, materials.logicProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
			addDescription( registry, materials.calcProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
			addDescription( registry, materials.engProcessorPress(), GuiText.inWorldCraftingPresses.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.InWorldFluix ) )
		{
			addDescription( registry, materials.fluixCrystal(), GuiText.inWorldFluix.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.InWorldSingularity ) )
		{
			addDescription( registry, materials.qESingularity(), GuiText.inWorldSingularity.getLocal() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.InWorldPurification ) )
		{
			addDescription( registry, materials.purifiedCertusQuartzCrystal(), GuiText.inWorldPurificationCertus.getLocal() );
			addDescription( registry, materials.purifiedNetherQuartzCrystal(), GuiText.inWorldPurificationNether.getLocal() );
			addDescription( registry, materials.purifiedFluixCrystal(), GuiText.inWorldPurificationFluix.getLocal() );
		}

	}

	private void addDescription( IModRegistry registry, IItemDefinition itemDefinition, String message )
	{
		itemDefinition.maybeStack( 1 ).ifPresent( itemStack -> registry.addDescription( itemStack, message ) );
	}

	private void registerGrinderRecipes( IDefinitions definitions, IModRegistry registry )
	{

		ItemStack grindstone = definitions.blocks().grindstone().maybeStack( 1 ).orElse( null );

		if( grindstone == null )
		{
			return;
		}

		registry.addRecipes( AEApi.instance().registries().grinder().getRecipes() );
		registry.addRecipeHandlers( new GrinderRecipeHandler() );
		registry.addRecipeCategories( new GrinderRecipeCategory( registry.getJeiHelpers().getGuiHelper() ) );
		registry.addRecipeCategoryCraftingItem( grindstone, GrinderRecipeCategory.UID );
	}

	private void registerCondenserRecipes( IDefinitions definitions, IModRegistry registry )
	{

		ItemStack condenser = definitions.blocks().condenser().maybeStack( 1 ).orElse( null );
		if( condenser == null )
		{
			return;
		}

		ItemStack matterBall = definitions.materials().matterBall().maybeStack( 1 ).orElse( null );
		if( matterBall != null )
		{
			registry.addRecipes( ImmutableList.of( CondenserOutput.MATTER_BALLS ) );
		}

		ItemStack singularity = definitions.materials().singularity().maybeStack( 1 ).orElse( null );
		if( singularity != null )
		{
			registry.addRecipes( ImmutableList.of( CondenserOutput.SINGULARITY ) );
		}

		if( matterBall != null || singularity != null )
		{
			registry.addRecipeCategories( new CondenserCategory( registry.getJeiHelpers().getGuiHelper() ) );
			registry.addRecipeCategoryCraftingItem( condenser, CondenserCategory.UID );
			registry.addRecipeHandlers( new CondenserOutputHandler( registry.getJeiHelpers().getGuiHelper(), matterBall, singularity ) );
		}
	}

	private void registerInscriberRecipes( IDefinitions definitions, IModRegistry registry )
	{

		registry.addRecipeHandlers( new InscriberRecipeHandler() );

		registry.addRecipeCategories( new InscriberRecipeCategory( registry.getJeiHelpers().getGuiHelper() ) );

		// Register the inscriber as the crafting item for the inscription category
		definitions.blocks().inscriber().maybeStack( 1 ).ifPresent( inscriber ->
		{
			registry.addRecipeCategoryCraftingItem( inscriber, InscriberRecipeCategory.UID );
		} );

		List<IInscriberRecipe> inscriberRecipes = new ArrayList<>( AEApi.instance().registries().inscriber().getRecipes() );
		registry.addRecipes( inscriberRecipes );
	}

	// Handle the generic crafting recipe for patterns in JEI
	private void registerFacadeRecipe( IDefinitions definitions, IModRegistry registry )
	{
		Optional<Item> itemFacade = definitions.items().facade().maybeItem();
		Optional<ItemStack> cableAnchor = definitions.parts().cableAnchor().maybeStack( 1 );
		if( itemFacade.isPresent() && cableAnchor.isPresent() )
		{
			registry.addRecipeRegistryPlugin( new FacadeRegistryPlugin( (ItemFacade) itemFacade.get(), cableAnchor.get() ) );
		}
	}

	@Override
	public void onRuntimeAvailable( IJeiRuntime jeiRuntime )
	{
		JEI jeiModule = (JEI) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.JEI );
		jeiModule.setJei( new JeiRuntimeAdapter( jeiRuntime ) );
	}
}
