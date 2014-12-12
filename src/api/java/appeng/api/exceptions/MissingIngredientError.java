package appeng.api.exceptions;

public class MissingIngredientError extends Exception {

	private static final long serialVersionUID = -998858343831371697L;

	public MissingIngredientError(String n) {
		super( n );
	}

}
