package appeng.core.api.definitions;


import net.minecraft.item.Item;

import com.google.common.base.Optional;

import appeng.api.definitions.IItemDefinition;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.core.FeatureHandlerRegistry;
import appeng.core.FeatureRegistry;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.ItemStackSrc;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;


public class DefinitionConstructor
{
	private final FeatureRegistry features;
	private final FeatureHandlerRegistry handlers;

	public DefinitionConstructor( FeatureRegistry features, FeatureHandlerRegistry handlers )
	{
		this.features = features;
		this.handlers = handlers;
	}

	public final AEItemDefinition registerAndConstructDefinition( IAEFeature feature )
	{
		final IFeatureHandler handler = feature.handler();

		if ( handler.isFeatureAvailable() )
		{
			this.handlers.addFeatureHandler( handler );
			this.features.addFeature( feature );
		}

		return handler.getDefinition();
	}

	public final AEColoredItemDefinition constructColoredDefinition( IItemDefinition target, int offset )
	{
		final ColoredItemDefinition definition = new ColoredItemDefinition();

		final Item item = target.item();

		for ( AEColor color : AEColor.VALID_COLORS )
		{
			definition.add( color, new ItemStackSrc( item, offset + color.ordinal() ) );
		}

		return definition;
	}

	public final AEColoredItemDefinition constructColoredDefinition( ItemMultiPart target, PartType type )
	{
		final ColoredItemDefinition definition = new ColoredItemDefinition();

		for ( AEColor color : AEColor.values() )
		{
			ItemStackSrc multiPartSource = target.createPart( type, color );
			final Optional<ItemStackSrc> maybeSource = Optional.fromNullable( multiPartSource );

			if ( maybeSource.isPresent() )
			{
				definition.add( color, multiPartSource );
			}
		}

		return definition;
	}
}
