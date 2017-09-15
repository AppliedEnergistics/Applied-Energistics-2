
package appeng.api.storage.data;


import appeng.api.config.FuzzyMode;


public interface IAEStackSearchKey<T> extends Comparable<IAEStackSearchKey<T>>
{
	IAEStackSearchKey<T> getLowerBound( final FuzzyMode fuzzy, final boolean ignoreMeta );

	IAEStackSearchKey<T> getUpperBound( final FuzzyMode fuzzy, final boolean ignoreMeta );

	T getDefinition();
}
