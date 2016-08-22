package appeng.bootstrap.components;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;


/**
 * Registers a custom state mapper for a given block.
 */
public class StateMapperComponent implements InitComponent
{

	private final Block block;

	private final IStateMapper stateMapper;

	public StateMapperComponent( Block block, IStateMapper stateMapper )
	{
		this.block = block;
		this.stateMapper = stateMapper;
	}

	@Override
	public void initialize( Side side )
	{
		ModelLoader.setCustomStateMapper( block, stateMapper );
		if( stateMapper instanceof IResourceManagerReloadListener )
		{
			( (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager() ).registerReloadListener( (IResourceManagerReloadListener) stateMapper );
		}
	}
}
