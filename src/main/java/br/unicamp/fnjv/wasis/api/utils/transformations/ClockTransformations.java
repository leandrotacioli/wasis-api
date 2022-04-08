package br.unicamp.fnjv.wasis.api.utils.transformations;

/**
 * Biblioteca Global - Transformação de horas
 *
 * @author Leandro Tacioli
 */
public class ClockTransformations {

    /**
     * Transforma tempo (em milisegundos) no formato de relógio digital (ex: 01:02,399)
     *
     * @param timeMilliseconds - Tempo em milisegundos
     *
     * @return timeTransformed
     */
    public static String millisecondsIntoDigitalFormat(int timeMilliseconds) {
        String timeTransformed;

        int timeSeconds = Math.round(timeMilliseconds / 1000);

        if (timeMilliseconds > 0) {
            int minutes = (int) Math.floor(timeSeconds / 60);
            int hours = (int) Math.floor(minutes / 60);
            minutes = minutes - hours * 60;
            int seconds = timeSeconds - minutes * 60 - hours * 3600;

            String time = String.format("%03d", timeMilliseconds);
            String milliseconds = time.substring(time.length() - 3);

            timeTransformed = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + "." + milliseconds;
        } else {
            timeTransformed = "00:00:00.000";
        }

        return timeTransformed;
    }

}