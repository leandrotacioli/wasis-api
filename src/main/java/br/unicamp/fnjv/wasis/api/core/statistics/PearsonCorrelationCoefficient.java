package br.unicamp.fnjv.wasis.api.core.statistics;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula a <i>Coeficiente de Correlação de Pearson</i> entre duas amostras.
 *
 * @author Leandro Tacioli
 */
public class PearsonCorrelationCoefficient {

    private Map<Integer, Double> xSample;

    private Map<Integer, Double> ySample;

    /** Quantidade mínima de registros (chaves) que deverá ser considerada na comparação das amostras */
    private int minRecords = 0;

    /**
     * Retorna a amostra X final que foi considerada no cálculo da Correlação de Pearson.
     *
     * @return xSample
     */
    public Map<Integer, Double> getXSample() {
        return xSample;
    }

    /**
     * Retorna a amostra Y final que foi considerada no cálculo da Correlação de Pearson.
     *
     * @return ySample
     */
    public Map<Integer, Double> getYSample() {
        return ySample;
    }

    /**
     * <pre>
     * Calcula a <i>Correlação de Pearson</i> entre duas amostras.
     *
     * Dados dos parâmetros:
     * <i>Chave (Integer) - Ex: Frequência</i>
     * <i>Valor (Double)  - Ex: Decibel</i>
     * </pre>
     *
     * @param xSample - Amostra X
     * @param ySample - Amostra Y
     */
    public PearsonCorrelationCoefficient(Map<Integer, Double> xSample, Map<Integer, Double> ySample) {
        this(xSample, ySample, 0);
    }

    /**
     * <pre>
     * Calcula a <i>Correlação de Pearson</i> entre duas amostras.
     *
     * Dados dos parâmetros:
     * <i>Chave (Integer) - Ex: Frequência</i>
     * <i>Valor (Double)  - Ex: Decibel</i>
     * </pre>
     *
     * @param xSample - Amostra X
     * @param ySample - Amostra Y
     * @param minRecords - Quantidade mínima de registros (chaves) que deverá ser considerada na comparação das amostras
     */
    public PearsonCorrelationCoefficient(Map<Integer, Double> xSample, Map<Integer, Double> ySample, int minRecords) {
        this.xSample = xSample;
        this.ySample = ySample;
        this.minRecords = minRecords;
    }

    /**
     * <pre>
     * Calcula o coeficiente de correlação.
     *
     * Regras:
     * 1 - O tamanho das duas amostras serão comparadas e a quantidade de chaves a serem calculados será do tamanho da menor amostra;
     * 2 - Serão comparados os valores das mesmas chaves nas duas amostras;
     * 3 - Valida a quantidade mínima de registros caso tenha sido informada.
     * </pre>
     *
     * @return correlationValue
     */
    public double calculateCorrelationCoefficient() {
        List<PearsonCorrelationSample> samples = new ArrayList<>();

        // Tamanho da amostra X é maior que da amostra Y
        if (xSample.size() > ySample.size()) {
            ySample.forEach((keyY, valueY) -> {
                xSample.forEach((keyX, valueX) -> {
                    if (keyX == keyY) {
                        samples.add(new PearsonCorrelationSample(keyX, valueX, valueY));
                    }
                });
            });

        // Tamanho da amostra X é menor que da amostra Y
        } else if (xSample.size() < ySample.size()) {
            xSample.forEach((keyX, valueX) -> {
                ySample.forEach((keyY, valueY) -> {
                    if (keyX == keyY) {
                        samples.add(new PearsonCorrelationSample(keyX, valueX, valueY));
                    }
                });
            });

        // Tamanho da amostra X é igual ao da amostra Y
        } else if (xSample.size() == ySample.size()) {
            ySample.forEach((keyY, valueY) -> {
                xSample.forEach((keyX, valueX) -> {
                    if (keyX == keyY) {
                        samples.add(new PearsonCorrelationSample(keyX, valueX, valueY));
                    }
                });
            });
        }

        // Atribui valores aos arrays X e Y
        double[] arrayXSample = new double[samples.size()];
        double[] arrayYSample = new double[samples.size()];

        xSample = new HashMap<>();
        ySample = new HashMap<>();

        for (int indexSample = 0; indexSample < samples.size(); indexSample++) {
            arrayXSample[indexSample] = samples.get(indexSample).getValueX();
            arrayYSample[indexSample] = samples.get(indexSample).getValueY();

            xSample.put(samples.get(indexSample).getKey(), samples.get(indexSample).getValueX());
            ySample.put(samples.get(indexSample).getKey(), samples.get(indexSample).getValueY());
        }

        // Realiza o cálculo da correlação
        double correlationValue = 0;

        if (minRecords == 0 || (minRecords > 0 && samples.size() >= minRecords)) {
            double sumXSample = calculateSum(arrayXSample);
            double sumSquareXSample = calculateSumSquare(arrayXSample);

            double sumYSample = calculateSum(arrayYSample);
            double dblSumSquareYSample = calculateSumSquare(arrayYSample);

            double sumXYSamples = multiplyArrays(arrayXSample, arrayYSample);

            double value1 = arrayXSample.length * sumXYSamples;
            double value2 = sumXSample * sumYSample;

            double value3 = Math.sqrt(arrayXSample.length * sumSquareXSample - sumXSample * sumXSample);
            double value4 = Math.sqrt(arrayYSample.length * dblSumSquareYSample - sumYSample * sumYSample);

            correlationValue = (value1 - value2) / (value3 * value4);
        }

        if (Double.isNaN(correlationValue)) {
            correlationValue = 0;
        }

        return correlationValue;
    }

    /**
     * Retorna o coeficiente de determinação (porcentagem).
     *
     * @param correlationValue
     *
     * @return determinationCoefficient
     */
    public double calculateDeterminationCoefficient(double correlationValue) {
        return correlationValue * correlationValue;
    }

    /**
     * Calcula a soma de todos os elementos de um array.
     *
     * @param array
     *
     * @return sumArray
     */
    private double calculateSum(double[] array) {
        double sumArray = 0;

        for (int i = 0; i < array.length; i++) {
            sumArray += array[i];
        }

        return sumArray;
    }

    /**
     * Eleva ao quadrado todos os elementos de um array e calcula a soma.
     *
     * @param array
     *
     * @return sumSquare
     */
    private double calculateSumSquare(double[] array) {
        double sumSquare = 0;

        for (int i = 0; i < array.length; i++) {
            sumSquare += array[i] * array[i];
        }

        return sumSquare;
    }

    /**
     * Multiplica os valores dos arrays (ex: = X1 * Y1) e calcula a soma.
     *
     * @param arrayXSample
     * @param arrayYSample
     *
     * @return sumMultiply
     */
    private double multiplyArrays(double[] arrayXSample, double[] arrayYSample) {
        double sumMultiply = 0;

        for (int i = 0; i < arrayXSample.length; i++) {
            sumMultiply += arrayXSample[i] * arrayYSample[i];
        }

        return sumMultiply;
    }

    // **********************************************************************************************
    /**
     * Atribui valores de um mesma chave das amostras X e Y.
     */
    @Data
    class PearsonCorrelationSample {

        private int key;
        private double valueX;
        private double valueY;

        /**
         * Atribui valores de um mesma chave das amostras X e Y.
         *
         * @param key
         * @param valueX
         * @param valueY
         */
        private PearsonCorrelationSample(int key, double valueX, double valueY) {
            this.key = key;
            this.valueX = valueX;
            this.valueY = valueY;
        }

    }
}