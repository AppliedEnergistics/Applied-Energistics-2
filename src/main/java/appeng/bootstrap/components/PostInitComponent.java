package appeng.bootstrap.components;


import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface PostInitComponent extends IBootstrapComponent
{

	@Override
	void postInitialize( Side side );
}
