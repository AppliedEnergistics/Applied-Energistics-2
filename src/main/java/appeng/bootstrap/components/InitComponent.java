package appeng.bootstrap.components;


import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;


@FunctionalInterface
public interface InitComponent extends IBootstrapComponent
{

	@Override
	void initialize( Side side );

}
