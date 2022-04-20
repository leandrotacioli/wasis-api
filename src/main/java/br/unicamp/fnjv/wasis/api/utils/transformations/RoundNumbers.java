package br.unicamp.fnjv.wasis.api.utils.transformations;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundNumbers {

	/**
	 * Round a number to N decimal places.
	 *
	 * @param value - Value to be rounded
	 * @param decimalPlaces - Number of decimal places
	 *
	 * @return roundedValue
	 */
	public static double round(double value, int decimalPlaces) {
		return round(value, decimalPlaces, RoundingMode.HALF_UP);
	}

	/**
	 * Round a number to N decimal places.
	 *
	 * @param value - Value to be rounded
	 * @param decimalPlaces - Number of decimal places
	 * @param roundingMode - Rounding mode
	 *
	 * @return roundedValue
	 */
	public static double round(double value, int decimalPlaces, RoundingMode roundingMode) {
		return BigDecimal.valueOf(value).setScale(decimalPlaces, roundingMode).doubleValue();
	}

}