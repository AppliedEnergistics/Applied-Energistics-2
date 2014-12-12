package appeng.api.parts;

public enum CableRenderMode
{

	Standard(false),

	CableView(true);

	public final boolean transparentFacades;
	public final boolean opaqueFacades;

	private CableRenderMode(boolean hideFacades) {
		this.transparentFacades = hideFacades;
		opaqueFacades = !hideFacades;
	}
}
