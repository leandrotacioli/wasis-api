package br.unicamp.fnjv.wasis.api.core.dsp.features;

/**
 * Feature Extraction.
 *
 * @author Leandro Tacioli
 */
public abstract class FeatureExtraction {

    /**
     * Take samples from an audio signal and computes the features.
     *
     * @param audioSignal
     */
    public abstract void process(double[] audioSignal);

    /**
     * Computes the features from audio frames.
     * <br>
     * It assumes that framing have already been performed.
     *
     * @param frames
     */
    public abstract void processFrames(double[][] frames);

    /**
     * Returns the feature coefficients.
     */
    public abstract double[][] getFeature();

    /**
     * Returns the mean of the coefficients.
     */
    public abstract double[] getMean();

    /**
     * Returns the standard deviation of the coefficients.
     */
    public abstract double[] getStandardDeviation();

    /**
     * Retorna uma string com todos os coeficientes de uma feature concatenados.
     * <br>
     * Obs: Os coeficientes são separados por um ';'.
     *
     * @param featureVector
     *
     * @return featureCoefficients
     */
    public static String getFeatureCoefficients(double[] featureVector) {
        StringBuffer featureCoefficients = new StringBuffer();

        for (int indexValue = 0; indexValue < featureVector.length; indexValue++) {
            featureCoefficients.append(featureVector[indexValue] + ((indexValue < featureVector.length - 1) ? ";" : ""));
        }

        return featureCoefficients.toString();
    }

}