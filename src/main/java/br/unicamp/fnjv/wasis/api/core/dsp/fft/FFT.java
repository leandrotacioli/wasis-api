package br.unicamp.fnjv.wasis.api.core.dsp.fft;

/**
 * Transforma amostra do domínio de tempo para o domínio de frequência.
 *
 * @author Leandro Tacioli
 */
public class FFT {

    private FFTColumbia fftColumbia;

    /**
     * Transforma amostra do domínio de tempo para o domínio de frequência.
     *
     * @param fftSampleSize  - Número de amostras da FFT (Potência de 2)
     */
    public FFT(int fftSampleSize) {
        this.fftColumbia = new FFTColumbia(fftSampleSize);
    }

    /**
     * <pre>
     * Executa a FFT, transformando amostra no domínio de tempo para o domínio de frequência.
     *
     * <b>IMPORTANTE:</b> Desejável executar a função de janelamento <i>applyWindow()</i> antes desta operação.
     * </pre>
     *
     * @param timeData - Amostra no domínio de tempo
     */
    public void executeFFT(double[] timeData) {
        fftColumbia.fft(timeData);
    }

    /**
     * <pre>
     * Retorna a parte real.
     *
     * <b>IMPORTANTE:</b> Os primeiros valores correspondem às frequências mais baixas, enquanto os últimos valores correspondem às frequências mais altas.
     * </pre>
     *
     * @return real
     */
    public double[] getReal() {
        return fftColumbia.getReal();
    }

    /**
     * <pre>
     * Retorna a parte imaginária.
     *
     * <b>IMPORTANTE:</b> Os primeiros valores correspondem às frequências mais baixas, enquanto os últimos valores correspondem às frequências mais altas.
     * </pre>
     *
     * @return imag
     */
    public double[] getImag() {
        return fftColumbia.getImag();
    }

    /**
     * <pre>
     * Retorna as amplitudes em dBFS.
     *
     * <b>IMPORTANTE:</b> Os primeiros valores são correspondentes às frequências mais baixas, enquanto os últimos valores são correspondentes às frequências mais altas.
     * </pre>
     *
     * @return amplitude
     */
    public double[] getAmplitudes() {
        return fftColumbia.getAmplitudes();
    }

}