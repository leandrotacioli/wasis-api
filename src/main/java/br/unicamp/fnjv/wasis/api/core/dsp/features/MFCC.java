package br.unicamp.fnjv.wasis.api.core.dsp.features;

import br.unicamp.fnjv.wasis.api.core.dsp.fft.FFT;
import br.unicamp.fnjv.wasis.api.core.dsp.windowing.WindowFunction;
import br.unicamp.fnjv.wasis.api.utils.mathematics.MatrixOperations;
import br.unicamp.fnjv.wasis.api.utils.statistics.BasicStatistics;
import br.unicamp.fnjv.wasis.api.utils.transformations.RoundNumbers;

/**
 * Feature extraction class used to extract Mel-Frequency Cepstral Coefficients (MFFC) from audio signals.
 *
 * @author Leandro Tacioli
 */
public class MFCC extends FeatureExtraction {

    /**
     * Number of MFCCs coefficients per frame.
     * <br>
     * <b>IMPORTANT 1:</b> The 0th coefficient will be discarded because it is considered as a collection of average energies of the frequency bands.
     * <br>
     * <b>IMPORTANT 2:</b> Delta & Delta Delta will also be computed, returning a total of 36 coefficients.
     */
    private final int MFFC_COEFFICIENTS = 13;

    /** Number of Mel filters */
    private final int MEL_FILTERS = 23;

    /** Number of samples per frame */
    private final int FRAME_LENGTH = 1024;

    /** Window Function */
    private final String WINDOW_FUNCTION = WindowFunction.HAMMING;

    /** Lower limit of the filter */
    private final double LOWER_FILTER_FREQUENCY = 45.0;

    /** Sample rate */
    private double sampleRate;

    /** Delta N */
    private final int DELTA_N = 2;

    /** Arithmetic progression of Delta */
    private double[][] deltaProgression;

    /** Delta denominator */
    private double deltaDenominator;

    /** MFCC Coefficients */
    private double[][] mfcc;

    /** The mean of the MFCC coefficients. */
    private double[] mean;

    /** The standard deviation of the LPC coefficients. */
    private double[] standardDeviation;

    /**
     * Feature extraction class used to extract Mel-Frequency Cepstral Coefficients (MFFCs) from audio signals.
     *
     * @param sampleRate
     */
    public MFCC(double sampleRate) {
        this.sampleRate = sampleRate;

        calculateDeltaValues();
    }

    /**
     * Calculates Arithmetic progression and denominator of Delta.
     */
    private void calculateDeltaValues() {
        // Calculate Delta Arithmetic Progression
        // Ex: {-2, -1, 0, 1, 2} - when N = 2
        deltaProgression = new double[1][2 * DELTA_N + 1];

        for (int indexProgression = -DELTA_N; indexProgression <= DELTA_N; indexProgression++) {
            deltaProgression[0][indexProgression + DELTA_N] = indexProgression;
        }

        // Calculate Delta Denominator
        deltaDenominator = 0;

        for (int indexDelta = 0; indexDelta <= DELTA_N; indexDelta++) {
            deltaDenominator += Math.pow(indexDelta, 2);
        }

        deltaDenominator = 2 * deltaDenominator;
    }

    /**
     * <pre>
     * Take samples from an audio signal and computes the Mel-Frequency Cepstral Coefficients (MFCCs).
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
     * Computes the Mel-Frequency Cepstral Coefficients (MFCCs) from audio frames.
     *
     * It assumes that pre-emphasis and framing have already been performed.
     * </pre>
     *
     * @param frames
     */
    @Override
    public void processFrames(double[][] frames) {
        WindowFunction windowFunction = new WindowFunction(WINDOW_FUNCTION);

        // 0th coefficient will be discarded, hence 'MFFC_COEFFICIENTS - 1'
        // Total of static coefficients - Not considering Delta and Delta-Delta
        int totalStaticCoefficients = MFFC_COEFFICIENTS - 1;

        // Initializes the MFCC matrix
        double[][] initialMfcc = new double[frames.length][totalStaticCoefficients];
        double[] magnitudeSpectrum;
        double[] melFilterBank;
        double[] naturalLogarithm;
        double[] cepstralCoefficients;

        // Below computations are all based on individual frames
        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            // Step 3 - Windowing - Apply Hamming Window to all frames
            frames[indexFrame] = windowFunction.applyWindow(frames[indexFrame]);

            // Step 4 - Magnitude Spectrum (FFT)
            magnitudeSpectrum = magnitudeSpectrum(frames[indexFrame]);

            // Step 5 - Mel Filter Bank
            melFilterBank = melFilterBank(magnitudeSpectrum);

            // Step 6 - Logarithm
            naturalLogarithm = naturalLogarithm(melFilterBank);

            // Step 7 - DCT - Cepstral coefficients
            cepstralCoefficients = cepstralCoefficients(naturalLogarithm);

            // Add resulting MFCC to array
            // 0th coefficient is discarded
            for (int indexCoefficient = 1; indexCoefficient < MFFC_COEFFICIENTS; indexCoefficient++) {
                initialMfcc[indexFrame][indexCoefficient - 1] = cepstralCoefficients[indexCoefficient];
            }
        }

        // Step 8 - Delta & Delta Delta
        double[][] delta = performDelta(initialMfcc);   // Differential Coefficients
        double[][] deltaDelta = performDelta(delta);    // Acceleration Coefficients

        // Step 9 - Final MFCC feature
        int totalMfccCoefficients = totalStaticCoefficients * 3;   // Considering Delta and Delta-Delta
        mfcc = new double[frames.length][totalMfccCoefficients];

        // Concatenates MFCC + Delta + Delta Delta
        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            for (int indexCoefficient = 0; indexCoefficient < totalStaticCoefficients; indexCoefficient++) {
                mfcc[indexFrame][indexCoefficient] = RoundNumbers.round(initialMfcc[indexFrame][indexCoefficient], 4);                              // MFCC
                mfcc[indexFrame][indexCoefficient + totalStaticCoefficients] = RoundNumbers.round(delta[indexFrame][indexCoefficient], 4);          // Delta
                mfcc[indexFrame][indexCoefficient + totalStaticCoefficients * 2] = RoundNumbers.round(deltaDelta[indexFrame][indexCoefficient], 4); // Delta Delta
            }
        }

        // Calculates mean and standard deviation for each coefficient
        if (frames.length > 1) {
            mean = new double[totalMfccCoefficients];
            standardDeviation = new double[totalMfccCoefficients];

            for (int indexCoefficient = 0; indexCoefficient < totalMfccCoefficients; indexCoefficient++) {
                double[] coefficientValues = new double[frames.length];

                for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
                    coefficientValues[indexFrame] = mfcc[indexFrame][indexCoefficient];
                }

                mean[indexCoefficient] = RoundNumbers.round(BasicStatistics.mean(coefficientValues), 4);
                standardDeviation[indexCoefficient] = RoundNumbers.round(BasicStatistics.standardDeviation(coefficientValues), 4);
            }
        }
    }

    /**
     * Returns the final MFCC Coefficients.
     *
     * @return mfcc
     */
    @Override
    public double[][] getFeature() {
        return mfcc;
    }

    /**
     * Returns the mean of the MFCC coefficients.
     *
     * @return mean
     */
    @Override
    public double[] getMean() {
        return mean;
    }

    /**
     * Returns the standard deviation of the MFCC coefficients.
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
     * Calculates the Mel filter bank.
     *
     * @param magnitudeSpectrum
     *
     * @return melFilterBank
     */
    private double[] melFilterBank(double[] magnitudeSpectrum) {
        int[] fftBinIndices = fftBinIndices();

        double[] temp = new double[MEL_FILTERS + 2];

        for (int k = 1; k <= MEL_FILTERS; k++) {
            double num1 = 0;
            double num2 = 0;

            for (int i = fftBinIndices[k - 1]; i <= fftBinIndices[k]; i++) {
                num1 += ((i - fftBinIndices[k - 1] + 1) / (fftBinIndices[k] - fftBinIndices[k - 1] + 1)) * magnitudeSpectrum[i];
            }

            for (int i = fftBinIndices[k] + 1; i <= fftBinIndices[k + 1]; i++) {
                num2 += (1 - ((i - fftBinIndices[k]) / (fftBinIndices[k + 1] - fftBinIndices[k] + 1))) * magnitudeSpectrum[i];
            }

            temp[k] = num1 + num2;
        }

        double[] melFilterBank = new double[MEL_FILTERS];

        for (int i = 0; i < MEL_FILTERS; i++) {
            melFilterBank[i] = temp[i + 1];
        }

        return melFilterBank;
    }

    /**
     * Calculates the FFT bin indices.
     *
     * @return fftBinIndices
     */
    private int[] fftBinIndices() {
        int[] fftBinIndices = new int[MEL_FILTERS + 2];

        fftBinIndices[0] = (int) Math.round(LOWER_FILTER_FREQUENCY / sampleRate * FRAME_LENGTH);
        fftBinIndices[fftBinIndices.length - 1] = FRAME_LENGTH / 2;

        for (int indexMelFilter = 1; indexMelFilter <= MEL_FILTERS; indexMelFilter++) {
            fftBinIndices[indexMelFilter] = (int) Math.round(centerFrequency(indexMelFilter) / sampleRate * FRAME_LENGTH);
        }

        return fftBinIndices;
    }

    /**
     * Computes the natural logarithm.
     *
     * @param melFilterBank - Mel Filter Bank
     *
     * @return naturalLogarithm
     */
    private double[] naturalLogarithm(double[] melFilterBank) {
        double[] naturalLogarithm = new double[melFilterBank.length];

        final double FLOOR = -50.0;

        for (int i = 0; i < melFilterBank.length; i++) {
            naturalLogarithm[i] = Math.log(melFilterBank[i]);

            // check if ln() returns a value less than the floor
            if (naturalLogarithm[i] < FLOOR) {
                naturalLogarithm[i] = FLOOR;
            }
        }

        return naturalLogarithm;
    }

    /**
     * Cepstral coefficients are calculated from the Mel log powers (DCT).
     *
     * @param naturalLogarithm
     *
     * @return cepstralCoefficients
     */
    private double[] cepstralCoefficients(double[] naturalLogarithm) {
        double[] cepstralCoefficients = new double[MFFC_COEFFICIENTS];

        for (int i = 0; i < cepstralCoefficients.length; i++) {
            for (int j = 1; j <= MEL_FILTERS; j++) {
                cepstralCoefficients[i] += naturalLogarithm[j - 1] * Math.cos(Math.PI * i / MEL_FILTERS * (j - 0.5));
            }
        }

        return cepstralCoefficients;
    }

    /**
     * Calculates logarithm with base 10.
     *
     * @param value - Number to take the log of
     *
     * @return log10
     */
    private double log10(double value) {
        return Math.log(value) / Math.log(10);
    }

    /**
     * Calculates center frequency.
     *
     * @param indexMelFilter - Index of Mel filters
     *
     * @return centerFrequency
     */
    private double centerFrequency(int indexMelFilter) {
        double[] mel = new double[2];
        mel[0] = frequencyToMel(LOWER_FILTER_FREQUENCY);
        mel[1] = frequencyToMel(sampleRate / 2);

        // take inverse mel of:
        double valueToInvert = mel[0] + ((mel[1] - mel[0]) / (MEL_FILTERS + 1)) * indexMelFilter;

        return inverseMel(valueToInvert);
    }

    /**
     * Calculates the inverse of Mel Frequency.
     *
     * @param melFrequency
     *
     * @return inverseMelFrequency
     */
    private double inverseMel(double melFrequency) {
        return 700 * (Math.pow(10, melFrequency / 2595) - 1);
    }

    /**
     * Convert Frequency to Mel-Frequency.
     *
     * @param frequency
     *
     * @return melFrequency
     */
    private double frequencyToMel(double frequency) {
        return 2595 * log10(1 + frequency / 700);
    }

    /**
     * Performs Delta computation.
     * <br>
     * <i>Delta</i>       - Differential Coefficient.
     * <br>
     * <i>Delta Delta</i> - Acceleration Coefficient.
     *
     * @param data
     *
     * @return delta
     */
    private double[][] performDelta(double[][] data) {
        double[][] delta = new double[data.length][data[0].length];

        double[][] paddedData = padding(data);

        double[][] paddedToProcess;
        double[][] numerator;

        for (int indexFrame = 0; indexFrame < delta.length; indexFrame++) {
            int indexPaddedToProcess = 0;

            paddedToProcess = new double[2 * DELTA_N + 1][data[0].length];

            for (int indexPadded = indexFrame; indexPadded < indexFrame + 2 * DELTA_N + 1; indexPadded++) {
                paddedToProcess[indexPaddedToProcess++] = paddedData[indexPadded];
            }

            numerator = MatrixOperations.multiplyMatrices(deltaProgression, paddedToProcess);

            for (int indexNumerator = 0; indexNumerator < numerator[0].length; indexNumerator++) {
                numerator[0][indexNumerator] = numerator[0][indexNumerator] / deltaDenominator;
            }

            delta[indexFrame] = numerator[0];
        }

        return delta;
    }

    /**
     * Pad out the data by repeating the border values, according to the <i>DELTA_N</i> value.
     *
     * @param data
     *
     * @return paddedData
     */
    private double[][] padding(double[][] data) {
        int totalRows = data.length;
        int totalColumns = data[0].length;
        int totalPaddedRows = data.length + (DELTA_N * 2);

        double[][] paddedData = new double[totalPaddedRows][totalColumns];

        // Initial & final padding
        for (int i = 0; i < DELTA_N; i++) {
            paddedData[i] = data[0];                                    // Initial padding
            paddedData[totalRows + DELTA_N + i] = data[totalRows - 1];  // Final padding
        }

        // Middle
        for (int i = 0; i < totalRows; i++) {
            paddedData[i + DELTA_N] = data[i];
        }

        return paddedData;
    }

}