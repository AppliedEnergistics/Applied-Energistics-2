package appeng.me.storage;


import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.TickRateModulation;


public interface ITickingMonitor
{

	TickRateModulation onTick();

	void setActionSource( BaseActionSource actionSource );

}
