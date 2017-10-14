
package appeng.core.stats;


import appeng.bootstrap.ICriterionTriggerRegistry;


public class AdvancementTriggers
{
	private AppEngAdvancementTrigger networkApprentice = new AppEngAdvancementTrigger( "network_apprentice" );
	private AppEngAdvancementTrigger networkEngineer = new AppEngAdvancementTrigger( "network_engineer" );
	private AppEngAdvancementTrigger networkAdmin = new AppEngAdvancementTrigger( "network_admin" );
	private AppEngAdvancementTrigger spatialExplorer = new AppEngAdvancementTrigger( "spatial_explorer" );

	public AdvancementTriggers( ICriterionTriggerRegistry registry )
	{
		registry.register( networkApprentice );
		registry.register( networkEngineer );
		registry.register( networkAdmin );
		registry.register( spatialExplorer );
	}

	public IAdvancementTrigger getNetworkApprentice()
	{
		return this.networkApprentice;
	}

	public IAdvancementTrigger getNetworkEngineer()
	{
		return this.networkEngineer;
	}

	public IAdvancementTrigger getNetworkAdmin()
	{
		return this.networkAdmin;
	}

	public IAdvancementTrigger getSpatialExplorer()
	{
		return this.spatialExplorer;
	}
}
