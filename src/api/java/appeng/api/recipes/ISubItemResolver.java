package appeng.api.recipes;

public interface ISubItemResolver
{

	/**
	 * @param namespace namespace of sub item
	 * @param fullName name of sub item
	 * @return either a ResolveResult, or a ResolverResultSet
	 */
	public Object resolveItemByName(String namespace, String fullName);

}
