package br.unicamp.fnjv.wasis.api.core.dsp.windowing;

/**
 * Funções de janelamento - Aprimora as características espectrais de uma amostra de sinal.
 *
 * @author Leandro Tacioli
 */
public class WindowFunction {

    public static final String BARTLETT = "BARTLETT";
    public static final String BLACKMAN = "BLACKMAN";
    public static final String HAMMING = "HAMMING";
    public static final String HANNING = "HANNING";
    public static final String RECTANGULAR = "RECTANGULAR";

    private static final int BARTLETT_WINDOW = 0;
    private static final int BLACKMAN_WINDOW = 1;
    private static final int HAMMING_WINDOW = 2;
    private static final int HANNING_WINDOW = 3;
    private static final int RECTANGULAR_WINDOW = 4;

    public static final Object[][] WINDOW_FUNCTIONS = new Object[][]{{BARTLETT,        BLACKMAN,        HAMMING,        HANNING,        RECTANGULAR},
                                                                     {BARTLETT_WINDOW, BLACKMAN_WINDOW, HAMMING_WINDOW, HANNING_WINDOW, RECTANGULAR_WINDOW}};

    private int windowType = HANNING_WINDOW;     // Padrão = HANNING

    /**
     * Funções de janelamento - Aprimora as características espectrais de uma amostra de sinal.
     *
     * @param window
     */
    public WindowFunction(String window) {
        setWindowType(window);
    }

    /**
     * Altera a função de janelamento.
     *
     * @param window -  BARTLETT / BLACKMAN / HAMMING / HANNING / RECTANGULAR
     */
    public void setWindowType(String window) {
        for (int index = 0; index < WINDOW_FUNCTIONS[0].length; index++) {
            if (window.toUpperCase().equals(WINDOW_FUNCTIONS[0][index])) {
                windowType = (int) WINDOW_FUNCTIONS[1][index];
            }
        }
    }

    /**
     * Aprimora as características espectrais de frames de sinal.
     *
     * @param frames
     *
     * @return frames - Frames com função de janelamento aplicada
     */
    public double[][] applyWindow(double[][] frames) {
        for (int indexFrame = 0; indexFrame < frames.length; indexFrame++) {
            frames[indexFrame] = applyWindow(frames[indexFrame]);
        }

        return frames;
    }

    /**
     * Aprimora as características espectrais de uma amostra de sinal.
     *
     * @param data - Amostra
     *
     * @return data - Amostra com função de janelamento aplicada
     */
    public double[] applyWindow(double[] data) {
        int sampleSize = data.length;
        int m = sampleSize / 2;

        double r;
        double[] window = new double[sampleSize];

        switch (windowType) {

            // Bartlett window
            case BARTLETT_WINDOW:
                for (int n = 0; n < sampleSize; n++) {
                    window[n] = 1.0f - Math.abs(n - m) / m;
                }

                break;

            // Blackman window
            case BLACKMAN_WINDOW:
                r = Math.PI / m;

                for (int n = -m; n < m; n++) {
                    window[m + n] = 0.42f + 0.5f * Math.cos(n * r) + 0.08f * Math.cos(2 * n * r);
                }

                break;

            // Hamming window
            case HAMMING_WINDOW:
                r = Math.PI / m;

                for (int n = -m; n < m; n++) {
                    window[m + n] = 0.54f + 0.46f * Math.cos(n * r);
                }

                break;

            // Hanning window
            case HANNING_WINDOW:
                r = Math.PI / (m + 1);

                for (int n = -m; n < m; n++) {
                    window[m + n] = 0.5f + 0.5f * Math.cos(n * r);
                }

                break;

            // Rectangular function
            default:
                for (int n = 0; n < sampleSize; n++) {
                    window[n] = 1.0f;
                }

                break;

        }

        // Processes data
        for (int i = 0; i < data.length; i++) {
            data[i] *= window[i];
        }

        return data;
    }

}