package appeng.bootstrap.components;


import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface PreInitComponent extends IBootstrapComponent
{

	@Override
	void preInitialize( Side side );

}
