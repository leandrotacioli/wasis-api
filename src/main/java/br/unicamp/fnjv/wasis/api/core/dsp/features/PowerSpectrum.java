package br.unicamp.fnjv.wasis.api.core.dsp.features;

import br.unicamp.fnjv.wasis.api.core.dsp.fft.FFT;
import br.unicamp.fnjv.wasis.api.core.dsp.fft.FFTWindowFunction;
import br.unicamp.fnjv.wasis.api.utils.transformations.RoundNumbers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Feature extraction class used to extract Power Spectrum (PS) from audio signals.
 *
 * @author Leandro Tacioli
 */
public class PowerSpectrum extends FeatureExtraction {

    /** Number of samples per frame */
    private final int FRAME_LENGTH = 1024;

    /** Window Function */
    private final String WINDOW_FUNCTION = FFTWindowFunction.HAMMING;

    /** Maximum frequency (50% of the <i>sampleRate</i>) */
    private double maximumFrequency;

    /** Number of frequency samples (50% of the <i>FRAME_LENGTH</i>) */
    private double frequencySamples;

    /**
     * <pre>
     * Final Power Spectrum Coefficients
     *
     * ps[0][x] - Frequency values
     * ps[1][x] - Decibel values
     * </pre>
     */
    private double[][] powerSpectrum;

    /**
     * <pre>
     * Feature extraction class used to extract Power Spectrum (PS) from audio signals.
     *
     * OBS: Assumes initial frequency = 0 and final frequency = (<i>sampleRate / 2</i>).
     * </pre>
     *
     * @param sampleRate
     */
    public PowerSpectrum(double sampleRate) {
        this(sampleRate, 0, (int) sampleRate / 2);
    }

    /**
     * Feature extraction class used to extract Power Spectrum (PS) from audio signals.
     *
     * @param sampleRate
     * @param initialFrequency
     * @param finalFrequency
     */
    public PowerSpectrum(double sampleRate, int initialFrequency, int finalFrequency) {
        // ******************************************************************************************8
        // Initiliaze matrix of frequency (Hz) and intensity (dBFS)
        maximumFrequency = sampleRate / 2;          // Divides by 2: Nyquist-Shannon - Default Value = 22050Hz
        frequencySamples = FRAME_LENGTH / 2;        // Default value = 512

        // Computes the final number of coefficients filtering initial and final frequencies
        List<Integer> coefficients = new ArrayList<Integer>();

        int margin = (int) (maximumFrequency / frequencySamples);   // Margin to take an inferior and superior sample

        for (int indexFrequency = 0; indexFrequency < frequencySamples; indexFrequency++) {
            double frequency = maximumFrequency - (maximumFrequency / frequencySamples * indexFrequency);

            if ((frequency >= initialFrequency - margin) && (frequency <= finalFrequency + margin)) {
                coefficients.add((int) frequency);
            }
        }

        Collections.sort(coefficients); // Sort the coefficients from lowest to highest frequencies

        powerSpectrum = new double[2][coefficients.size()];

        for (int indexCoefficient = 0; indexCoefficient < coefficients.size(); indexCoefficient++) {
            powerSpectrum[0][indexCoefficient] = coefficients.get(indexCoefficient);
            powerSpectrum[1][indexCoefficient] = -1000;   // Initiate the coefficients with a very low decibel value
        }
    }

    /**
     * Take samples from an audio signal and computes the Power Spectrum (PS).
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
     * Computes the Power Spectrum (PS) from audio frames.
     *
     * OBS: It assumes that framing has already been performed.
     * </pre>
     *
     * @param frames
     */
    @Override
    public void processFrames(double[][] frames) {
        // Step 2 - Windowing - Apply Hamming Window to all frames
        FFT fft = new FFT(FRAME_LENGTH, WINDOW_FUNCTION);

        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            frames[indexFrame] = fft.applyWindow(frames[indexFrame]);
        }

        // Below computations are all based on individual frames
        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            // Step 3 - FFT
            fft.executeFFT(frames[indexFrame]);

            double[] amplitudes = fft.getAmplitudes();

            // Step 4 - Power Spectrum Coefficients
            int lastPowerSpectrumValue = 0;

            for (int indexFrequency = 0; indexFrequency < frequencySamples; indexFrequency++) {
                double frequency = maximumFrequency / frequencySamples * indexFrequency;
                frequency += maximumFrequency / frequencySamples;

                for (int indexPowerSpectrumValue = lastPowerSpectrumValue; indexPowerSpectrumValue < powerSpectrum[0].length; indexPowerSpectrumValue++) {
                    if ((int) frequency == powerSpectrum[0][indexPowerSpectrumValue]) {
                        double decibel = amplitudes[indexFrequency];

                        if (decibel > powerSpectrum[1][indexPowerSpectrumValue]) {
                            powerSpectrum[1][indexPowerSpectrumValue] = RoundNumbers.round(decibel, 4);

                            lastPowerSpectrumValue = indexPowerSpectrumValue;

                            break;
                        }
                    }
                }
            }
        }

        // Step 5 - Normalization of the Power Spectrum Coefficients
        // dBFS should accept only negative values
        // In case of positive, all the values are adjusted from the difference of the higher value
        double higherValue = -1000;

        for (int indexPowerSpectrumValue = 0; indexPowerSpectrumValue < powerSpectrum[0].length; indexPowerSpectrumValue++) {
            if (powerSpectrum[1][indexPowerSpectrumValue] > higherValue) {
                higherValue = powerSpectrum[1][indexPowerSpectrumValue];
            }
        }

        if (higherValue >= 0) {
            for (int indexPowerSpectrumValue = 0; indexPowerSpectrumValue < powerSpectrum[0].length; indexPowerSpectrumValue++) {
                powerSpectrum[1][indexPowerSpectrumValue] = RoundNumbers.round((powerSpectrum[1][indexPowerSpectrumValue] - higherValue), 4);
            }
        }
    }

    @Override
    public double[][] getFeature() {
        return powerSpectrum;
    }

    @Override
    public double[] getMean() {
        return null;
    }

    @Override
    public double[] getStandardDeviation() {
        return null;
    }

}