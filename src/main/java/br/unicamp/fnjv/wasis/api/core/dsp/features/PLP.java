package br.unicamp.fnjv.wasis.api.core.dsp.features;

import br.unicamp.fnjv.wasis.api.core.dsp.fft.FFT;
import br.unicamp.fnjv.wasis.api.core.dsp.windowing.WindowFunction;
import br.unicamp.fnjv.wasis.api.utils.statistics.BasicStatistics;
import br.unicamp.fnjv.wasis.api.utils.transformations.RoundNumbers;

/**
 * Feature extraction class used to extract Perceptual Linear Prediction (PLP) from audio signals.
 *
 * @author Leandro Tacioli
 */
public class PLP extends FeatureExtraction {

    /** Number of PLP filters. */
    private final int PLP_FILTERS = 21;

    /** LPC Order. Equivalent to the number of LPC coefficients. */
    private final int LPC_ORDER = 24;

    /** LPCC Order. Equivalent to the number of LPCC coefficients. Also, the final number of PLP coefficients. */
    private final int LPCC_ORDER = 24;

    /** Number of samples per frame. */
    private final int FRAME_LENGTH = 1024;

    /** Window Function */
    private final String WINDOW_FUNCTION = WindowFunction.HAMMING;

    /** Lower limit of the filter */
    private final double LOWER_FILTER_FREQUENCY = 45.0;

    /** Sample rate */
    private double sampleRate;

    /** Filter coefficients. */
    private BarkFilterbank[] barkFilterbanks;

    /** Equal Loudness. */
    private double[] equalLoudness;

    /** Minimum Bark Frequency. */
    private double minimumBarkFrequency;

    /** Maximum Bark Frequency. */
    private double maximumBarkFrequency;

    /** Delta Bark Frequency. */
    private double deltaBarkFrequency;

    /** Cosine values for Inverse Discrete Cosine Transform (IDCT).  */
    private double[][] cosine;

    /** PLP Coefficients */
    private double[][] plp;

    /** The mean of the PLP coefficients. */
    private double[] mean;

    /** The standard deviation of the LPC coefficients. */
    private double[] standardDeviation;

    /**
     * Feature extraction class used to extract Perceptual Linear Prediction (PLP) from audio signal.
     */
    public PLP(double sampleRate) {
        this.sampleRate = sampleRate;

        computeCosine();
    }

    /**
     * <pre>
     * Takes an audio signal and computes the Perceptual Linear Prediction (PLP).
     *
     * It starts processing the samples by performing framing.
     * </pre>
     *
     * @param audioSignal
     */
    @Override
    public void process(double[] audioSignal) {
        // Step 1 - Frame Blocking
        double[][] frames = Preprocessing.framing(audioSignal, FRAME_LENGTH);

        processFrames(frames);
    }

    /**
     * <pre>
     * Computes the Perceptual Linear Prediction (PLP) from audio frames.
     *
     * It assumes that framing have already been performed.
     * </pre>
     *
     * @param frames
     */
    @Override
    public void processFrames(double[][] frames) {
        WindowFunction windowFunction = new WindowFunction(WINDOW_FUNCTION);

        minimumBarkFrequency = frequencyToBark(LOWER_FILTER_FREQUENCY);
        maximumBarkFrequency = frequencyToBark(sampleRate / 2);
        deltaBarkFrequency = (maximumBarkFrequency - minimumBarkFrequency) / (PLP_FILTERS + 1);

        plp = new double[frames.length][LPCC_ORDER];

        double[] magnitudeSpectrum;
        double[] plpSpectral;
        double[] intensityLoudness;
        double[] autoCorrelation;

        // Below computations are all based on individual frames
        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            // Step 2 - Windowing - Apply Hamming Window to all frames
            frames[indexFrame] = windowFunction.applyWindow(frames[indexFrame]);

            // Step 3 - Magnitude Spectrum (FFT)
            magnitudeSpectrum = magnitudeSpectrum(frames[indexFrame]);

            // Step 4 - Bark Filter Bank
            barkFilterBank(magnitudeSpectrum);

            // Step 5 - Equal Loudness / Pre-emphasis
            equalLoudness();

            // PLP Spectral array
            plpSpectral = new double[PLP_FILTERS];

            for (int indexFilter = 0; indexFilter < PLP_FILTERS; indexFilter++) {
                plpSpectral[indexFilter] = barkFilterbanks[indexFilter].filterOutput(magnitudeSpectrum);
                plpSpectral[indexFilter] *= equalLoudness[indexFilter];  // Scale for equal loudness preemphasis
            }

            // Step 6 - Intensity Loudness
            intensityLoudness = intensityLoudness(plpSpectral);

            autoCorrelation = applyCosine(intensityLoudness);

            // Step 7 - Linear Predictive Coding (LPC)
            LPCC lpcc = new LPCC(LPC_ORDER, LPCC_ORDER);
            lpcc.processAutocorrelatedFrame(autoCorrelation);

            // Step 8 - Linear Prediction Cepstral Coefficient (LPCC)
            plp[indexFrame] = lpcc.getFeature()[0];
        }

        // Calculates mean and standard deviation for each coefficient
        if (frames.length > 1) {
            mean = new double[LPCC_ORDER];
            standardDeviation = new double[LPCC_ORDER];

            for (int indexCoefficient = 0; indexCoefficient < LPCC_ORDER; indexCoefficient++) {
                double[] coefficientValues = new double[frames.length];

                for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
                    coefficientValues[indexFrame] = plp[indexFrame][indexCoefficient];
                }

                mean[indexCoefficient] = RoundNumbers.round(BasicStatistics.mean(coefficientValues), 4);
                standardDeviation[indexCoefficient] = RoundNumbers.round(BasicStatistics.standardDeviation(coefficientValues), 4);
            }
        }
    }

    /**
     * Returns the final PLP Coefficients.
     *
     * @return plp
     */
    @Override
    public double[][] getFeature() {
        return plp;
    }

    /**
     * Returns the mean of the PLP Coefficients.
     *
     * @return mean
     */
    @Override
    public double[] getMean() {
        return mean;
    }

    /**
     * Returns the standard deviation of the PLP Coefficients.
     *
     * @return standardDeviation
     */
    @Override
    public double[] getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Computes the magnitude spectrum of the input frame (FFT).
     *
     * @param frame - Input frame signal
     *
     * @return magnitudeSpectrum - Magnitude Spectrum
     */
    private double[] magnitudeSpectrum(double[] frame) {
        double[] magnitudeSpectrum = new double[frame.length];

        FFT fft = new FFT(FRAME_LENGTH);
        fft.executeFFT(frame);

        for (int k = 0; k < frame.length; k++) {
            magnitudeSpectrum[k] = Math.pow(fft.getReal()[k] * fft.getReal()[k] + fft.getImag()[k] * fft.getImag()[k], 0.5);
        }

        return magnitudeSpectrum;
    }

    /**
     * Computes BarkFilterBank.
     *
     * @param magnitudeSpectrum
     */
    private void barkFilterBank(double[] magnitudeSpectrum) {
        double[] frequencyBins = new double[FRAME_LENGTH];

        for (int indexFrameLength = 0; indexFrameLength < FRAME_LENGTH; indexFrameLength++) {
            frequencyBins[indexFrameLength] = (indexFrameLength * (sampleRate / 2)) / (FRAME_LENGTH - 1);
        }

        barkFilterbanks = new BarkFilterbank[PLP_FILTERS];

        for (int indexFilter = 0; indexFilter < PLP_FILTERS; indexFilter++) {
            double centerFrequency = barkToFrequency(minimumBarkFrequency + indexFilter * deltaBarkFrequency);

            barkFilterbanks[indexFilter] = new BarkFilterbank(frequencyBins, centerFrequency);
        }
    }

    /**
     * Create an array of equal loudness preemphasis scaling terms for all the filters.
     */
    private void equalLoudness() {
        equalLoudness = new double[PLP_FILTERS];

        for (int indexFilter = 0; indexFilter < PLP_FILTERS; indexFilter++) {
            double centerFrequency = barkFilterbanks[indexFilter].getCenterFrequency();

            equalLoudness[indexFilter] = loudnessScalingFunction(centerFrequency);
        }
    }

    /**
     * <pre>
     * This function return the equal loudness preemphasis factor at any frequency.
     * The preemphasis function is given by:
     *
     * E(w) = f^4 / (f^2 + 1.6e5) ^ 2 * (f^2 + 1.44e6) / (f^2 + 9.61e6)
     *
     * This is more modern one from HTK, for some reason it's preferred over old variant, and it doesn't require conversion to radians
     *
     * E(w) = (w^2+56.8e6)*w^4/((w^2+6.3e6)^2(w^2+0.38e9)(w^6+9.58e26))
     *
     * where w is frequency in radians/second
     * </pre>
     *
     * @param frequency
     */
    private double loudnessScalingFunction(double frequency) {
        double fSq = frequency * frequency;
        double fSub = fSq / (fSq + 1.6e5);

        return fSub * fSub * ((fSq + 1.44e6) / (fSq + 9.61e6));
    }

    /**
     * Applies the intensity loudness power law. This operation is an approximation to the power law of hearing and
     * simulates the non-linear relationship between sound intensity and percieved loudness. Computationally, this
     * operation is used to reduce the spectral amplitude of the critical band to enable all-pole modeling with
     * relatively low order AR filters.
     *
     * @param plpSpectrum
     */
    private double[] intensityLoudness(double[] plpSpectrum) {
        double[] intensityLoudness = new double[plpSpectrum.length];

        for (int i = 0; i < plpSpectrum.length; i++) {
            intensityLoudness[i] = Math.pow(plpSpectrum[i], 1.0 / 3.0);
        }

        return intensityLoudness;
    }

    /**
     * Compute the Cosine values for IDCT.
     */
    private void computeCosine() {
        cosine = new double[LPC_ORDER + 1][PLP_FILTERS];

        double period = (double) 2 * PLP_FILTERS;

        for (int i = 0; i <= LPC_ORDER; i++) {
            double frequency = 2 * Math.PI * i / period;

            for (int j = 0; j < PLP_FILTERS; j++) {
                cosine[i][j] = Math.cos(frequency * (j + 0.5));
            }
        }
    }

    /**
     * Compute the Discrete Cosine Transform for the given power spectrum.
     *
     * @param plpSpectrum - PLP Spectrum
     *
     * @return autoCorrelation
     */
    private double[] applyCosine(double[] plpSpectrum) {
        double[] autoCorrelation = new double[LPC_ORDER + 1];
        double period = PLP_FILTERS;
        double beta = 0.5;

        // Apply the IDCT
        for (int i = 0; i <= LPC_ORDER; i++) {
            if (PLP_FILTERS > 0) {
                int j = 0;

                autoCorrelation[i] += (beta * plpSpectrum[j] * cosine[i][j]);

                for (j = 1; j < PLP_FILTERS; j++) {
                    autoCorrelation[i] += (plpSpectrum[j] * cosine[i][j]);
                }

                autoCorrelation[i] /= period;
            }
        }

        return autoCorrelation;
    }

    /**
     * Convert Frequency (Hz) to Bark-Frequency.
     *
     * @param frequency
     *
     * @return barkFrequency
     */
    private double frequencyToBark(double frequency) {
        double x = frequency / 600;

        return (6.0 * Math.log(x + Math.sqrt(x * x + 1)));
    }

    /**
     * Convert Bark-Frequency to Frequency (Hz).
     *
     * @param barkFrequency
     *
     * @return frequency
     */
    private double barkToFrequency(double barkFrequency) {
        double x = barkFrequency / 6.0;

        return (300.0 * (Math.exp(x) - Math.exp(-x)));
    }

    /**
     * Bark Filterbank.
     */
    class BarkFilterbank {

        private double[] filterCoefficients;
        private double centerFrequency;

        /**
         * Return the center frequency.
         *
         * @return centerFrequency
         */
        protected double getCenterFrequency() {
            return centerFrequency;
        }

        /**
         * Bark Filterbank.
         *
         * @param frequencyBins
         * @param centerFrequency
         */
        private BarkFilterbank(double[] frequencyBins, double centerFrequency) {
            this.centerFrequency = centerFrequency;

            filterCoefficients = new double[FRAME_LENGTH];

            double centerBarkFrequency = frequencyToBark(centerFrequency);

            for (int indexFrameLength = 0; indexFrameLength < FRAME_LENGTH; indexFrameLength++) {
                double barkFrequency = frequencyToBark(frequencyBins[indexFrameLength]) - centerBarkFrequency;

                if (barkFrequency < -2.5) {
                    filterCoefficients[indexFrameLength] = 0.0;
                } else if (barkFrequency <= -0.5) {
                    filterCoefficients[indexFrameLength] = Math.pow(10.0, barkFrequency + 0.5);
                } else if (barkFrequency <= 0.5) {
                    filterCoefficients[indexFrameLength] = 1.0;
                } else if (barkFrequency <= 1.3) {
                    filterCoefficients[indexFrameLength] = Math.pow(10.0, -2.5 * (barkFrequency - 0.5));
                } else {
                    filterCoefficients[indexFrameLength] = 0.0;
                }
            }
        }

        /**
         * Compute the PLP spectrum at the center frequency of this filter for a given power spectrum.
         *
         * @param spectrum - Input power spectrum to be filtered
         *
         * @return plpSpectrum - PLP spectrum value
         */
        public double filterOutput(double[] spectrum) {
            double plpSpectrum = 0.0;

            for (int i = 0; i < FRAME_LENGTH; i++) {
                plpSpectrum += spectrum[i] * filterCoefficients[i];
            }

            return plpSpectrum;
        }

    }

}