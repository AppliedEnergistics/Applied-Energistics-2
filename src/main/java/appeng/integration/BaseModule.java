package appeng.integration;

public abstract class BaseModule implements IIntegrationModule {

	protected void TestClass( Class clz )
	{
		 clz.isInstance(this);
	}

	@Override
	public abstract void Init() throws Throwable;

	@Override
	public abstract  void PostInit();
	
}
