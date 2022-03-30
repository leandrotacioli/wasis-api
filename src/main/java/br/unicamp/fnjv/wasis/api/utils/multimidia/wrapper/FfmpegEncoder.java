package br.unicamp.fnjv.wasis.api.utils.multimidia.wrapper;

import br.unicamp.fnjv.wasis.api.utils.multimidia.wav.AudioWavFormat;

import java.io.File;
import java.io.IOException;

/**
 * FFMPEG Encoder.
 * 
 * @author Leandro Tacioli
 */
public class FfmpegEncoder {

	private FfmpegExecutable ffmpegExecutable;     // Executável do FFMPEG utilizado pelo encoder
	private FfmpegWrapper ffmpegWrapper;

	private final String TARGET_FORMAT = "wav";
	private final String TARGET_CODEC = "pcm_s16le";

	private final int TARGET_SAMPLE_RATE = (int) AudioWavFormat.TARGET_SAMPLE_RATE;
	private final int TARGET_CHANNELS = AudioWavFormat.TARGET_CHANNEL_MONO;
	
	/**
	 * FFMPEG Encoder.
	 */
	public FfmpegEncoder() {
		ffmpegExecutable = new FfmpegExecutable();
	}

	/**
	 * Converte o arquivo baseado nos atributos passados como parâmetros.
	 * 
	 * @param fileSource - Arquivo original que será convertido
	 * @param fileTarget - Arquivo final já convertido
	 * 
	 * @throws Exception
	 */
	public void encode(File fileSource, File fileTarget) throws Exception {
		try {
			// Cria o Wrapper
			ffmpegWrapper = ffmpegExecutable.createWrapper();
			
			// Source File
			ffmpegWrapper.addParameter("-i");
			ffmpegWrapper.addParameter(fileSource.getAbsolutePath());
			
			// Target Format (WAV)
			ffmpegWrapper.addParameter("-f");
			ffmpegWrapper.addParameter(TARGET_FORMAT);
			
			// Target Codec
			ffmpegWrapper.addParameter("-acodec");
			ffmpegWrapper.addParameter(TARGET_CODEC);
			
			// Target Sample Rate
			ffmpegWrapper.addParameter("-ar");
			ffmpegWrapper.addParameter(String.valueOf(TARGET_SAMPLE_RATE));
			
			// Target Channels
			ffmpegWrapper.addParameter("-ac");
			ffmpegWrapper.addParameter(String.valueOf(TARGET_CHANNELS));
			
			// Target File
			ffmpegWrapper.addParameter("-y");
			ffmpegWrapper.addParameter(fileTarget.getAbsolutePath());
		
			// Executa o FFMPEG
			ffmpegWrapper.executeFfmpeg();

		} catch (Exception e) {
			throw e;
		}
	}
}