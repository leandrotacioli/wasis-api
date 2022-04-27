package br.unicamp.fnjv.wasis.api.core.dsp.features;

import br.unicamp.fnjv.wasis.api.core.dsp.windowing.WindowFunction;
import br.unicamp.fnjv.wasis.api.utils.statistics.BasicStatistics;
import br.unicamp.fnjv.wasis.api.utils.transformations.RoundNumbers;

/**
 * Feature extraction class used to extract Linear Predictive Coding (LPC) from audio signals.
 *
 * @author Leandro Tacioli
 */
public class LPC extends FeatureExtraction {

    /**
     * <pre>
     * Default LPC Order - Number of coefficients.
     * Used when the number of coefficients is not informed.
     * </pre>
     */
    protected final int DEFAULT_LPC_ORDER = 24;

    /** Final Order - Number of coefficients. */
    protected int lpcOrder;

    /** Number of samples per frame. */
    protected final int FRAME_LENGTH = 1024;

    /** Window Function */
    private final String WINDOW_FUNCTION = WindowFunction.HAMMING;

    /** Lambda. */
    private final double LAMBDA = 0.0;

    /** Reflection coefficients - Each row is related to its respective frame. */
    protected double[][] reflectionCoefficients;

    /** Autoregressive parameters - Each row is related to its respective frame. */
    protected double[][] autoregressiveParameters;

    /** Alpha - Energy of the frame - Each column is related to its respective frame. */
    protected double[] alpha;

    /** LPC coefficients. */
    private double[][] lpc;

    /** The mean of the LPC coefficients. */
    private double[] lpcMean;

    /** The standard deviation of the LPC coefficients. */
    private double[] lpcStandardDeviation;

    /**
     * Feature extraction class used to extract Linear Predictive Coding (LPC) from audio signals.
     */
    public LPC() {
        this.lpcOrder = DEFAULT_LPC_ORDER;
    }

    /**
     * Feature extraction class used to extract Linear Predictive Coding (LPC) from audio signals.
     *
     * @param order - LPC Order - Equivalent to the number of LPC coefficients
     */
    public LPC(int order) {
        this.lpcOrder = order;
    }

    /**
     * <pre>
     * Take samples from an audio signal and computes the Linear Predictive Coding (LPC) and LPCC (Linear Prediction Cepstral Coefficients).
     *
     * It starts processing the samples by performing pre-emphasis and framing.
     * </pre>
     *
     * @param audioSignal
     */
    @Override
    public void process(double[] audioSignal) {
        // Step 1 - Pre-Emphasis
        double[] preEmphasis = Preprocessing.preEmphasis(audioSignal);

        // Step 2 - Frame Blocking
        double[][] frames = Preprocessing.framing(preEmphasis, FRAME_LENGTH);

        processFrames(frames);
    }

    /**
     * <pre>
     * Computes the Linear Predictive Coding (LPC) from audio frames.
     *
     * It assumes that pre-emphasis and framing have already been performed.
     * </pre>
     *
     * @param frames
     */
    @Override
    public void processFrames(double[][] frames) {
        WindowFunction windowFunction = new WindowFunction(WINDOW_FUNCTION);

        reflectionCoefficients = new double[frames.length][lpcOrder + 1];
        autoregressiveParameters = new double[frames.length][lpcOrder + 1];
        alpha = new double[frames.length];

        // Below computations are all based on individual frames
        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            // Step 3 - Windowing - Apply Hamming Window to all frames
            frames[indexFrame] = windowFunction.applyWindow(frames[indexFrame]);

            // Step 4 - Autocorrelation
            double[] autoCorrelation = autoCorrelation(frames[indexFrame]);

            // Step 5 - Levinson-Durbin Algorithm
            levinsonDurbin(indexFrame, autoCorrelation);
        }

        computeFinalLPC(frames.length);
    }

    /**
     * Returns the final LPC coefficients.
     *
     * @return lpc
     */
    @Override
    public double[][] getFeature() {
        return lpc;
    }

    /**
     * Returns the mean of the LPC coefficients.
     *
     * @return lpcMean
     */
    @Override
    public double[] getMean() {
        return lpcMean;
    }

    /**
     * Returns the standard deviation of the LPC coefficients.
     *
     * @return lpcStandardDeviation
     */
    @Override
    public double[] getStandardDeviation() {
        return lpcStandardDeviation;
    }

    /**
     * Find the order-P autocorrelation array for the sequence x of length L and warping of lambda.
     *
     * @param samples
     *
     * @return autoCorrelation array
     */
    private double[] autoCorrelation(double[] samples) {
        double[] r = new double[lpcOrder + 1];
        double[] dl = new double[samples.length];
        double[] rt = new double[samples.length];
        double r1, r2, r1t;

        r[0] = 0;
        rt[0] = 0;
        r1 = 0;
        r2 = 0;

        for (int k = 0; k < samples.length; k++) {
            rt[0] += samples[k] * samples[k];

            dl[k] = r1 - LAMBDA * (samples[k] - r2);
            r1 = samples[k];
            r2 = dl[k];
        }

        for (int i = 1; i < r.length; i++) {
            rt[i] = 0;
            r1 = 0;
            r2 = 0;

            for (int k = 0; k < samples.length; k++) {
                rt[i] += dl[k] * samples[k];

                r1t = dl[k];
                dl[k] = r1 - LAMBDA * (r1t - r2);
                r1 = r1t;
                r2 = dl[k];
            }
        }

        for (int i = 0; i < r.length; i++) {
            r[i] = rt[i];
        }

        return r;
    }

    /**
     * <pre>
     * Method to compute Linear Prediction Coefficients for a frame using the Levinson-Durbin algorithm.
     * Assumes the following sign convention:
     * <i>prediction(x[t]) = Sum_i {Ar[i] * x[t-i]}</i>
     * </pre>
     *
     * @param indexFrame      - Index of the frame
     * @param autoCorrelation - Autocorrelation array
     */
    protected void levinsonDurbin(int indexFrame, double[] autoCorrelation) {
        double[] backwardPredictor = new double[lpcOrder + 1];

        alpha[indexFrame] = autoCorrelation[0];
        reflectionCoefficients[indexFrame][1] = -autoCorrelation[1] / autoCorrelation[0];
        autoregressiveParameters[indexFrame][0] = 1.0;
        autoregressiveParameters[indexFrame][1] = reflectionCoefficients[indexFrame][1];
        alpha[indexFrame] *= (1 - reflectionCoefficients[indexFrame][1] * reflectionCoefficients[indexFrame][1]);

        for (int i = 2; i <= lpcOrder; i++) {
            for (int j = 1; j < i; j++) {
                backwardPredictor[j] = autoregressiveParameters[indexFrame][i - j];
            }

            reflectionCoefficients[indexFrame][i] = 0;

            for (int j = 0; j < i; j++) {
                reflectionCoefficients[indexFrame][i] -= autoregressiveParameters[indexFrame][j] * autoCorrelation[i - j];
            }

            reflectionCoefficients[indexFrame][i] /= alpha[indexFrame];

            for (int j = 1; j < i; j++) {
                autoregressiveParameters[indexFrame][j] += reflectionCoefficients[indexFrame][i] * backwardPredictor[j];
            }

            autoregressiveParameters[indexFrame][i] = reflectionCoefficients[indexFrame][i];
            alpha[indexFrame] *= (1 - reflectionCoefficients[indexFrame][i] * reflectionCoefficients[indexFrame][i]);
        }
    }

    /**
     * Computes the final Linear Predictive Coding (LPC) coefficients.
     *
     * @param totalFrames
     */
    private void computeFinalLPC(int totalFrames) {
        lpc = new double[totalFrames][lpcOrder];

        for (int indexFrame = 0; indexFrame < totalFrames; indexFrame++) {
            for (int indexOrder = 0; indexOrder < lpcOrder; indexOrder++) {
                lpc[indexFrame][indexOrder] = RoundNumbers.round(autoregressiveParameters[indexFrame][indexOrder + 1], 4);
            }
        }

        // Calculates mean and standard deviation for each LPC coefficient
        if (totalFrames > 1) {
            lpcMean = new double[lpcOrder];
            lpcStandardDeviation = new double[lpcOrder];

            for (int indexCoefficient = 0; indexCoefficient < lpcOrder; indexCoefficient++) {
                double[] coefficientValues = new double[totalFrames];

                for (int indexFrame = 0; indexFrame < totalFrames; indexFrame++) {
                    coefficientValues[indexFrame] = lpc[indexFrame][indexCoefficient];
                }

                lpcMean[indexCoefficient] = RoundNumbers.round(BasicStatistics.mean(coefficientValues), 4);
                lpcStandardDeviation[indexCoefficient] = RoundNumbers.round(BasicStatistics.standardDeviation(coefficientValues), 4);
            }
        }
    }

}