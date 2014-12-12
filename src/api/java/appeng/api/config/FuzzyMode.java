package appeng.api.config;

public enum FuzzyMode
{
	// Note that percentage damaged, is the inverse of percentage durability.
	IGNORE_ALL(-1), PERCENT_99(0), PERCENT_75(25), PERCENT_50(50), PERCENT_25(75);

	final public float breakPoint;
	final public float percentage;

	private FuzzyMode(float p) {
		percentage = p;
		breakPoint = p / 100.0f;
	}

	public int calculateBreakPoint(int maxDamage)
	{
		return (int) ((percentage * maxDamage) / 100.0f);
	}

}