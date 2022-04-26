package br.unicamp.fnjv.wasis.api.utils.statistics;

/**
 * Library of basic statistical functions (Mean, Standard Deviation, Sample Variance).
 *
 * @author Leandro Tacioli
 */
public class BasicStatistics {

    /**
     * Library of basic statistical functions (Mean, Standard Deviation, Sample Variance).
     */
    private BasicStatistics() {

    }

    /**
     * Returns the sum of all values in the specified array.
     *
     * @param arrayValues
     *
     * @return the sum
     */
    private static double sum(double[] arrayValues) {
        validateNotNull(arrayValues);

        double sum = 0.0;

        for (int i = 0; i < arrayValues.length; i++) {
            sum += arrayValues[i];
        }

        return sum;
    }

    /**
     * Returns the average value in the specified array.
     *
     * @param arrayValues
     *
     * @return the average value
     */
    public static double mean(double[] arrayValues) {
        validateNotNull(arrayValues);

        if (arrayValues.length == 0) {
            return Double.NaN;
        }

        return sum(arrayValues) / arrayValues.length;
    }

    /**
     * Returns the sample standard deviation in the specified array.
     *
     * @param arrayValues
     *
     * @return the standard deviation
     */
    public static double standardDeviation(double[] arrayValues) {
        validateNotNull(arrayValues);

        return Math.sqrt(sampleVariance(arrayValues));
    }

    /**
     * Returns the sample variance in the specified array.
     *
     * @param arrayValues
     *
     * @return the sample variance
     */
    public static double sampleVariance(double[] arrayValues) {
        validateNotNull(arrayValues);

        if (arrayValues.length == 0) {
            return Double.NaN;
        }

        double average = mean(arrayValues);
        double sum = 0.0;

        for (int i = 0; i < arrayValues.length; i++) {
            sum += (arrayValues[i] - average) * (arrayValues[i] - average);
        }

        return sum / (arrayValues.length - 1);
    }

    /**
     * Throw an IllegalArgumentException if <i>object</i> is null
     *
     * @param object
     */
    private static void validateNotNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Argument is null");
        }
    }

}