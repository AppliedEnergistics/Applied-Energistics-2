package appeng.api.config;

public enum AccessRestriction
{
	NO_ACCESS(0), READ(1), WRITE(2), READ_WRITE(3);

	private final int permissionBit;

	private AccessRestriction(int v) {
		permissionBit = v;
	}

	public boolean hasPermission(AccessRestriction ar)
	{
		return (permissionBit & ar.permissionBit) == ar.permissionBit;
	}

	public AccessRestriction restrictPermissions(AccessRestriction ar)
	{
		return getPermByBit( permissionBit & ar.permissionBit);
	}

	public AccessRestriction addPermissions(AccessRestriction ar)
	{
		return getPermByBit( permissionBit | ar.permissionBit);
	}

	public AccessRestriction removePermissions(AccessRestriction ar)
	{
		return getPermByBit( permissionBit & (~ar.permissionBit) );
	}

	private AccessRestriction getPermByBit(int bit)
	{
		switch (bit)
		{
		default:
		case 0:
			return NO_ACCESS;
		case 1:
			return READ;
		case 2:
			return WRITE;
		case 3:
			return READ_WRITE;
		}
	}
}