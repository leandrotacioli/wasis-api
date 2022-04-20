package br.unicamp.fnjv.wasis.api.core.dsp.features;

/**
 * Audio Feature Preprocessing.
 *
 * @author Leandro Tacioli
 */
public class Preprocessing {

    /**
     * Pre-Emphasis Alpha (Set to 0 if no pre-emphasis should be performed)
     */
    private final static double PRE_EMPHASIS_ALPHA = 0.95;

    /**
     * Number of samples per frame.
     */
    private final static int FRAME_LENGTH = 1024;

    /**
     * Number of overlapping samples (Usually 50% of the <i>FRAME_LENGTH</i>).
     */
    private final static int OVERLAP_SAMPLES = FRAME_LENGTH / 2;

    /**
     * Audio Feature Preprocessing.
     */
    private Preprocessing() {

    }

    /**
     * Perform pre-emphasis to equalize amplitude of high and low frequency.
     *
     * @param audioSignal - Audio Signal
     *
     * @return preEmphasis
     */
    public static double[] preEmphasis(double[] audioSignal) {
        double[] preEmphasis = new double[audioSignal.length];

        for (int indexSignal = 1; indexSignal < audioSignal.length; indexSignal++) {
            preEmphasis[indexSignal] = audioSignal[indexSignal] - PRE_EMPHASIS_ALPHA * audioSignal[indexSignal - 1];
        }

        return preEmphasis;
    }

    /**
     * <pre>
     * Performs Frame Blocking to break down an audio signal into frames.
     *
     * Default <i>FRAME_LENGTH</i> = 1024.
     * Default <i>OVERLAP_SAMPLES</i> = FRAME_LENGTH / 2.
     * </pre>
     *
     * @param audioSignal - Audio Signal
     */
    public static double[][] framing(double[] audioSignal) {
        return framing(audioSignal, FRAME_LENGTH, OVERLAP_SAMPLES);
    }

    /**
     * Performs Frame Blocking to break down an audio signal into frames.
     *
     * @param audioSignal    - Audio Signal
     * @param frameLength    - Frame Length
     * @param overlapSamples - Overlap Samples
     */
    public static double[][] framing(double[] audioSignal, int frameLength, int overlapSamples) {
        double numFrames = (double) audioSignal.length / (double) (frameLength - overlapSamples);

        // unconditionally round up
        if ((numFrames / (int) numFrames) != 1) {
            numFrames = (int) numFrames + 1;
        }

        // use zero padding to fill up frames with not enough samples
        double[] paddedSignal = new double[(int) numFrames * frameLength];

        for (int indexSignal = 0; indexSignal < audioSignal.length; indexSignal++) {
            paddedSignal[indexSignal] = audioSignal[indexSignal];
        }

        double[][] frames = new double[(int) numFrames][frameLength];

        // break down speech signal into frames with specified shift interval to create overlap
        for (int indexFrame = 0; indexFrame < numFrames; indexFrame++) {
            for (int indexLength = 0; indexLength < frameLength; indexLength++) {
                frames[indexFrame][indexLength] = paddedSignal[indexFrame * (frameLength - overlapSamples) + indexLength];
            }
        }

        return frames;
    }

}