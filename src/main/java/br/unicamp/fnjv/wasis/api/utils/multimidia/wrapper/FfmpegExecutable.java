package br.unicamp.fnjv.wasis.api.utils.multimidia.wrapper;

/**
 * Cria o caminho do FFMPEG executável, verificando o sistema operacional utilizado.
 * 
 * @author Leandro Tacioli
 */
public class FfmpegExecutable {
	private final String EXECUTABLE_PATH = "res/";
	private String ffmpegExecutablePath;

	/**
	 * Cria o caminho do FFMPEG executável, verificando o sistema operacional utilizado.
	 */
	protected FfmpegExecutable() {
		String os = System.getProperty("os.name").toLowerCase();
		
		String suffix = "";
		
		if (os.contains("windows")) {
			suffix = "ffmpeg-win.exe";
		} else if (os.contains("nux")) {
			suffix = "ffmpeg-linux";
		} else if (os.contains("mac")) {
			suffix = "ffmpeg-mac";
		}
		
		ffmpegExecutablePath = EXECUTABLE_PATH + suffix;
	}
	
	/**
	 * Retorna uma nova instância do 'FfmpegWrapper', pronto para ser utilizada na chamada do FFMPEG.
	 */
	protected FfmpegWrapper createWrapper() {
		return new FfmpegWrapper(ffmpegExecutablePath);
	}

}