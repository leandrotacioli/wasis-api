package br.unicamp.fnjv.wasis.api.core.dsp.features;

import br.unicamp.fnjv.wasis.api.utils.statistics.BasicStatistics;
import br.unicamp.fnjv.wasis.api.utils.transformations.RoundNumbers;

/**
 * Feature extraction class used to extract LPCC (Linear Prediction Cepstral Coefficients) from audio signals.
 *
 * @author Leandro Tacioli
 */
public class LPCC extends LPC {

    /**
     * <pre>
     * Default LPCC Order - Number of coefficients.
     * Used when the Number of coefficients is not informed.
     * </pre>
     */
    protected final int DEFAULT_LPCC_ORDER = 24;

    /** Final Order - Number of coefficients. */
    private int lpccOrder;

    /** LPCC coefficients. */
    private double[][] lpcc;

    /** The mean of the LPCC coefficients. */
    private double[] lpccMean;

    /** The standard deviation of the LPCC coefficients. */
    private double[] lpccStandardDeviation;

    /**
     * Feature extraction class used to extract LPCC (Linear Prediction Cepstral Coefficients) from audio signals.
     */
    public LPCC() {
        this.lpcOrder = DEFAULT_LPC_ORDER;
        this.lpccOrder = DEFAULT_LPCC_ORDER;
    }

    /**
     * Feature extraction class used to extract LPCC (Linear Prediction Cepstral Coefficients) from audio signals.
     *
     * @param lpcOrder  - LPC Order - Equivalent to the number of LPC coefficients
     * @param lpccOrder - LPCC Order - Equivalent to the number of LPCC coefficients
     */
    public LPCC(int lpcOrder, int lpccOrder) {
        this.lpcOrder = lpcOrder;
        this.lpccOrder = lpccOrder;
    }

    /**
     * <pre>
     * Take samples from an audio signal and computes the LPCC (Linear Prediction Cepstral Coefficients).
     *
     * It starts processing the samples by performing pre-emphasis and framing.
     * </pre>
     *
     * @param audioSignal
     */
    @Override
    public void process(double[] audioSignal) {
        double[] preEmphasis = Preprocessing.preEmphasis(audioSignal);

        double[][] frames = Preprocessing.framing(preEmphasis, FRAME_LENGTH);

        processFrames(frames);
    }

    /**
     * <pre>
     * Computes the LPCC (Linear Prediction Cepstral Coefficients) from audio frames.
     *
     * It assumes that pre-emphasis and framing have already been performed.
     * </pre>
     *
     * @param frames
     */
    @Override
    public void processFrames(double[][] frames) {
        super.processFrames(frames);

        computeFinalLPCC(frames.length);
    }

    /**
     * Returns the final LPCC coefficients.
     *
     * @return lpcc
     */
    @Override
    public double[][] getFeature() {
        return lpcc;
    }

    /**
     * Returns the mean of the LPCC coefficients.
     *
     * @return lpccMean
     */
    @Override
    public double[] getMean() {
        return lpccMean;
    }

    /**
     * Returns the standard deviation of the LPCC coefficients.
     *
     * @return lpccStandardDeviation
     */
    @Override
    public double[] getStandardDeviation() {
        return lpccStandardDeviation;
    }

    /**
     * <pre>
     * Computes the LPCC (Linear Prediction Cepstral Coefficients) from an autocorrelated frame.
     *
     * Used in the Perceptual Linear Prediction (PLP) computation.
     * </pre>
     *
     * @param autocorrelatedFrame
     */
    public void processAutocorrelatedFrame(double[] autocorrelatedFrame) {
        double[][] frames = new double[1][];
        frames[0] = autocorrelatedFrame;

        reflectionCoefficients = new double[1][lpcOrder + 1];
        autoregressiveParameters = new double[1][lpcOrder + 1];
        alpha = new double[1];

        super.levinsonDurbin(0, autocorrelatedFrame);

        computeFinalLPCC(frames.length);
    }

    /**
     * Computes the final LPCC (Linear Prediction Cepstral Coefficients).
     *
     * @param totalFrames
     */
    private void computeFinalLPCC(int totalFrames) {
        lpcc = new double[totalFrames][lpccOrder];

        for (int indexFrame = 0; indexFrame < totalFrames; indexFrame++) {
            lpcc[indexFrame][0] = RoundNumbers.round(Math.log(alpha[indexFrame]), 4);
            lpcc[indexFrame][1] = RoundNumbers.round(-autoregressiveParameters[indexFrame][1], 4);

            int i;
            double sum;

            for (i = 2; i < Math.min(lpccOrder, lpcOrder + 1); i++) {
                sum = i * autoregressiveParameters[indexFrame][i];

                for (int j = 1; j < i; j++) {
                    sum += autoregressiveParameters[indexFrame][j] * lpcc[indexFrame][i - j] * (i - j);
                }

                lpcc[indexFrame][i] = RoundNumbers.round((-sum / i), 4);
            }

            // Only if lpccOrder > lpcOrder + 1
            for (; i < lpccOrder; i++) {
                sum = 0;

                for (int j = 1; j <= lpcOrder; j++) {
                    sum += autoregressiveParameters[indexFrame][j] * lpcc[indexFrame][i - j] * (i - j);
                }

                lpcc[indexFrame][i] = RoundNumbers.round((-sum / i), 4);
            }
        }

        // Calculates mean and standard deviation for each LPCC coefficient
        if (totalFrames > 1) {
            lpccMean = new double[lpccOrder];
            lpccStandardDeviation = new double[lpccOrder];

            for (int indexCoefficient = 0; indexCoefficient < lpccOrder; indexCoefficient++) {
                double[] coefficientValues = new double[totalFrames];

                for (int indexFrame = 0; indexFrame < totalFrames; indexFrame++) {
                    coefficientValues[indexFrame] = lpcc[indexFrame][indexCoefficient];
                }

                lpccMean[indexCoefficient] = RoundNumbers.round(BasicStatistics.mean(coefficientValues), 4);
                lpccStandardDeviation[indexCoefficient] = RoundNumbers.round(BasicStatistics.standardDeviation(coefficientValues), 4);
            }
        }
    }

}