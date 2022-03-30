package br.unicamp.fnjv.wasis.api.utils.multimidia.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * FFMPEG Wrapper
 * 
 * @author Leandro Tacioli
 */
public class FfmpegWrapper {
	private String ffmpegExecutablePath;

	private Process process;
	private ArrayList<String> arrayParameters;
	
	/**
	 * FFMPEG Wrapper.
	 * 
	 * @param ffmpegExecutablePath - Caminho do FFMPEG executável
	 */
	protected FfmpegWrapper(String ffmpegExecutablePath) {
		this.ffmpegExecutablePath = ffmpegExecutablePath;
		this.arrayParameters = new ArrayList<String>();
	}

	/**
	 * Adiciona um parâmetro à chamada do executável FFMPEG.
	 * 
	 * @param parameter
	 */
	protected void addParameter(String parameter) {
		arrayParameters.add(parameter);
	}

	/**
	 * Executa o FFMPEG.
	 *
	 * @throws Exception
	 */
	protected void executeFfmpeg() throws Exception {
		int paramSize = arrayParameters.size();
		
		String[] command = new String[paramSize + 1];
		command[0] = ffmpegExecutablePath;

		for (int indexParam = 0; indexParam < paramSize; indexParam++) {
			command[indexParam + 1] = arrayParameters.get(indexParam);
		}
		
		try {
			process = Runtime.getRuntime().exec(command);

			Scanner scanner = new Scanner(process.getErrorStream());
	
	        // Encontra duração total do arquivo de áudio
	        Pattern durationPattern = Pattern.compile("(?<=Duration: )[^,]*");
	        String duration = scanner.findWithinHorizon(durationPattern, 0);
	        
	        if (duration == null) {
	        	throw new IOException("Invalid audio file");
	        }
	        
	        String[] durationSplit = duration.split(":");
	        double dblTotalSeconds = Integer.parseInt(durationSplit[0]) * 3600 +   // hours
	                          		 Integer.parseInt(durationSplit[1]) * 60 +     // minutes
	                          		 Double.parseDouble(durationSplit[2]);         // seconds

			Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
			String match;
			String[] matchSplit;

			while ((match = scanner.findWithinHorizon(timePattern, 0)) != null) {
				/*
				matchSplit = match.split(":");

				double progressInSeconds = Integer.parseInt(matchSplit[0]) * 3600 +   // hours
						                   Integer.parseInt(matchSplit[1]) * 60 +     // minutes
						                   Double.parseDouble(matchSplit[2]);         // seconds

				//System.out.println("Total: " + dblTotalSeconds + " - Progresso: " + dblProgressInSeconds);
				*/
			}

			// Progresso = 100%
			destroyFfmpeg();
	        
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Cancela a execução do FFMPEG.
	 */
	protected void destroyFfmpeg() {
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

}