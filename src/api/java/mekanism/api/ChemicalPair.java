package mekanism.api;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

/**
 * An input of gasses for recipe use.
 * @author aidancbrady
 *
 */
public class ChemicalPair
{
	/** The left gas of this chemical input */
	public GasStack leftGas;

	/** The right gas of this chemical input */
	public GasStack rightGas;

	/**
	 * Creates a chemical input with two defined gasses.
	 * @param left - left gas
	 * @param right - right gas
	 */
	public ChemicalPair(GasStack left, GasStack right)
	{
		leftGas = left;
		rightGas = right;
	}

	/**
	 * If this is a valid ChemicalPair
	 * @return
	 */
	public boolean isValid()
	{
		return leftGas != null && rightGas != null;
	}

	/**
	 * Whether or not the defined input contains the same gasses and at least the required amount of the defined gasses as this input.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	public boolean meetsInput(ChemicalPair input)
	{
		return meets(input) || meets(input.swap());
	}

	/**
	 * Swaps the right gas and left gas of this input.
	 * @return a swapped ChemicalInput
	 */
	public ChemicalPair swap()
	{
		return new ChemicalPair(rightGas, leftGas);
	}

	/**
	 * Draws the needed amount of gas from each tank.
	 * @param leftTank - left tank to draw from
	 * @param rightTank - right tank to draw from
	 */
	public void draw(GasTank leftTank, GasTank rightTank)
	{
		if(meets(new ChemicalPair(leftTank.getGas(), rightTank.getGas())))
		{
			leftTank.draw(leftGas.amount, true);
			rightTank.draw(rightGas.amount, true);
		}
		else if(meets(new ChemicalPair(rightTank.getGas(), leftTank.getGas())))
		{
			leftTank.draw(rightGas.amount, true);
			rightTank.draw(leftGas.amount, true);
		}
	}

	/**
	 * Whether or not one of this ChemicalInput's GasStack entry's gas type is equal to the gas type of the given gas.
	 * @param stack - stack to check
	 * @return if the stack's gas type is contained in this ChemicalInput
	 */
	public boolean containsType(GasStack stack)
	{
		if(stack == null || stack.amount == 0)
		{
			return false;
		}

		return stack.isGasEqual(leftGas) || stack.isGasEqual(rightGas);
	}

	/**
	 * Actual implementation of meetsInput(), performs the checks.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	private boolean meets(ChemicalPair input)
	{
		if(input == null || !input.isValid())
		{
			return false;
		}

		if(input.leftGas.getGas() != leftGas.getGas() || input.rightGas.getGas() != rightGas.getGas())
		{
			return false;
		}

		return input.leftGas.amount >= leftGas.amount && input.rightGas.amount >= rightGas.amount;
	}

	public ChemicalPair copy()
	{
		return new ChemicalPair(leftGas.copy(), rightGas.copy());
	}
}
