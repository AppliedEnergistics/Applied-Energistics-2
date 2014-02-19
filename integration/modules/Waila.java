package appeng.integration.modules;

import java.util.List;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import appeng.block.AEBaseBlock;
import appeng.core.AppEng;
import appeng.integration.BaseModule;

public class Waila  extends BaseModule implements IWailaDataProvider {
	
	public static void yourRegistrationMethod(IWailaRegistrar registrar)
	{
		registrar.registerHeadProvider( (Waila)AppEng.instance.getIntegration("Waila"), AEBaseBlock.class);
		registrar.registerBodyProvider( (Waila)AppEng.instance.getIntegration("Waila"), AEBaseBlock.class);
		registrar.registerTailProvider( (Waila)AppEng.instance.getIntegration("Waila"), AEBaseBlock.class);
	}
	
	@Override
	public void Init() throws Throwable {
		TestClass( IWailaDataProvider.class );
		TestClass( IWailaRegistrar.class );
        FMLInterModComms.sendMessage("Waila", "register", "mcp.mobius.waila_demo.ProviderDemo.callbackRegister");		
	}

	@Override
	public void PostInit() throws Throwable {
		// :P		
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		currenttip.add("AE2 Says Hi");
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {

		return currenttip;
	}

}
