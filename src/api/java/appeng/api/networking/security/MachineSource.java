package appeng.api.networking.security;

public class MachineSource extends BaseActionSource
{

	public final IActionHost via;

	@Override
	public boolean isMachine()
	{
		return true;
	}

	public MachineSource(IActionHost v) {
		via = v;
	}

}
