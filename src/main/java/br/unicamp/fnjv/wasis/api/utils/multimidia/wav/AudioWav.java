package br.unicamp.fnjv.wasis.api.utils.multimidia.wav;

import br.unicamp.fnjv.wasis.api.utils.crypto.SHA256;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;

/**
 * Carrega e processa os dados de um arquivo de áudio.
 * 
 * @author Leandro Tacioli
 */
public class AudioWav {

	@Getter
	private AudioWavHeader audioWavHeader;       // Especificações do header do arquivo WAV
	
    private AudioInputStream audioInputStream;   // Fluxo do áudio

	private byte[] wavData;                      // Array dos dados do arquivo WAV
    
    private int wavDataSize;                     // Tamanho total dos dados do arquivo WAV
    private int wavDataInitialPosition;          // Mostra a posição inicial que o array dos dados está ocupando
    private int wavDataFinalPosition;            // Mostra a posição final que o array dos dados está ocupando

	private boolean isEndOfFile = false;         // True = Todos os dados do arquivo foram lidos

	@Getter
	private String audioFilePath;                // Caminho do arquivo de áudio

	@Getter
	private String audioFileHash;                // Hash SHA-256 do arquivo de áudio

	@Getter
    private int numSamples;                      // Número de amostras do arquivo WAV

	@Getter
	private int numSamplesPerChannel;            // Número de amostras por canal do arquivo WAV
    
    /**
     * Tamanho máximo do array para não sobrecarregar a memória (16,777,216).
     */
    private final int BYTE_ARRAY_MAX_SIZE = (int) Math.pow(2, 24);
    
    /**
     * Comprimento do buffer (4096).
     */
    private final int BUFFER_LENGTH = (int) Math.pow(2, 12);
    
    /**
     * Carrega e processa os dados de um arquivo de áudio.
     * 
     * @param audioFilePath - Caminho do arquivo de áudio
     */
    public AudioWav(String audioFilePath) {
    	try {
			this.audioFilePath = audioFilePath;
			this.audioFileHash = SHA256.getHashFromFile(new File(audioFilePath));
		} catch (Error | Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Carrega header e dados do arquivo WAV.
     * Confirma se o arquivo tem o formato WAV correto, e se necessária é feita a conversão do arquivo para WAV padrão.
     *
     * @throws FileNotFoundException
     * @throws Exception 
     */
    public void loadAudio() throws FileNotFoundException, Exception {
    	try {
			audioWavHeader = new AudioWavHeader(new FileInputStream(audioFilePath));

	        if (audioWavHeader.loadHeader()) {

	        	if (audioWavHeader.getSampleRate() != AudioWavFormat.TARGET_SAMPLE_RATE || audioWavHeader.getBitsPerSample() != AudioWavFormat.TARGET_BIT_RATE) {
	        		throw new GeneralException(HttpStatus.BAD_REQUEST, "Especificações incorretas para carregamento do arquivo de áudio.");

	        	} else {
	    	        extractWavData();

					numSamples = wavDataSize / audioWavHeader.getBytesPerSample();
					numSamplesPerChannel = numSamples / audioWavHeader.getChannels();
	        	}

	        } else {
				throw new GeneralException(HttpStatus.BAD_REQUEST, "Especificações incorretas para carregamento do arquivo de áudio.");
	        }
	        
    	} catch (FileNotFoundException e) {
    		throw new FileNotFoundException();
	        
    	} catch (Exception e) {
    		throw new Exception(e);
    	}
    }
    
    /**
     * Fecha o arquivo de áudio.
     */
    public void closeAudio() {   
    	try {
            if (audioInputStream != null) {
            	audioInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extrai dados do arquivo WAV e armazena no array de bytes 'wavData'.
     */
    private void extractWavData() {
        try {
        	audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
			wavDataSize = getWavDataSize();
        	
        	// Em caso de um arquivo WAV muito longo, 'intWavDataCurrentMaxPosition' 
        	// é alterado para 'BYTE_ARRAY_MAX_SIZE' para não sobrecarregar a memória
        	// É necessário carregar o método 'extractWavDataChunk' para pegar os dados restantes.
			wavDataInitialPosition = 0;
			wavDataFinalPosition = wavDataSize;
	        if (wavDataFinalPosition > BYTE_ARRAY_MAX_SIZE) {
				wavDataFinalPosition = BYTE_ARRAY_MAX_SIZE;
	        }

	        wavData = new byte[wavDataFinalPosition];
	        
	        byte[] arrayBuffer = new byte[BUFFER_LENGTH];
	        
	        int index = 0;
	        
	        while (true) {
	            int bytesRead = audioInputStream.read(arrayBuffer);

	            if (bytesRead == -1) {
	                break;
	            } else {
        	        for (int indexBytesRead = 0; indexBytesRead < bytesRead; indexBytesRead++) {
        	        	if (index >= wavDataFinalPosition) {
        	        		break;
        	        	}
        	        	
        	        	wavData[index] = arrayBuffer[indexBytesRead];
						index++;
        	        }
    	        }
	        }

        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Extrai dados a partir de um pedaço específico do arquivo WAV e armazena no array de bytes 'wavData'.
     * Esse método é utilizado em caso de um arquivo WAV muito grande, 
     * então o arquivo é processado em diferentes partes para não sobrecarregar a CPU e memória.
     * 
     * @param initialChunk - Ponto inicial do pedaço específico do arquivo WAV
     */
    public void extractWavDataChunk(int initialChunk) {
        try {
			isEndOfFile = false;
        	
        	audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));

			initialChunk = getChunkDataPosition(initialChunk);
        	
        	int arraySize = BYTE_ARRAY_MAX_SIZE;

			wavDataInitialPosition = initialChunk;
			wavDataFinalPosition = initialChunk + BYTE_ARRAY_MAX_SIZE;
        	
        	if (wavDataFinalPosition > wavDataSize) {
				wavDataFinalPosition = wavDataSize;
				arraySize = wavDataFinalPosition - wavDataInitialPosition;
        	}
        	
	        wavData = new byte[arraySize];
	        
	        byte[] arrayBuffer = new byte[BUFFER_LENGTH];
	        
	        boolean isDataExtracted = false;
	        
	        int index = 0;
	        int totalBytesRead = 0;
	        
	        while (true) {
	            int bytesRead = audioInputStream.read(arrayBuffer);

	            if (bytesRead == -1) {
	                break;
	                
	            } else {
	            	if (totalBytesRead + bytesRead < initialChunk) {
						totalBytesRead += bytesRead;
	            		
	            	} else {
		            	for (int indexBytesRead = 0; indexBytesRead < bytesRead; indexBytesRead++) {
		            		if (totalBytesRead >= initialChunk) {
		            			if (index < arraySize) {
	            					wavData[index] = arrayBuffer[indexBytesRead];
									index++;
	        					} else {
									isDataExtracted = true;
	    	            			break;       // Finishes FOR loop
	        					}
		            		}

							totalBytesRead++;
		            	}
		            	
		            	if (isDataExtracted) {
		            		break;               // Finishes WHILE loop
		            	}
		            }
    	        }
	        }

        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retorna o tamanho total (em bytes) do arquivo WAV a ser processado.<br>
     * <br>
     * O tamanho do <i>Header</i> do arquivo é desconsiderado.
     * 
     * @return wavDataSize
     */
    public int getWavDataSize() {
    	int wavDataSize = 0;
    	
    	try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
			
	        byte[] arrayBuffer = new byte[BUFFER_LENGTH];
	        
	        while (true) {
	            int bytesRead = audioInputStream.read(arrayBuffer);

	            if (bytesRead == -1) {
	                break;
	            } else {
					wavDataSize += bytesRead;
	            }
	        }
	        
    	} catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
		}

    	return wavDataSize;
    }
    
  	/**
     * Retorna o tempo total do áudio em milisegundos.
     * 
     * @return totalTime
     */
    public int getTotalTimeInMilliseconds() {
        return (int) (wavDataSize * 1000L / audioWavHeader.getByteRate());
    }
    
    /**
     * Retorna a posição em bytes de um pedaço do áudio levando em consideração um pedaço da amostra.
     * 
     * @param chunk - Pedaço da amostra 'sample' baseado no tempo do áudio
     * 
     * @return chunkDataPosition
     */
    public int getChunkDataPosition(int chunk) {
    	return chunk * audioWavHeader.getChannels() * audioWavHeader.getBytesPerSample();
    }
    
    /**
     * Retorna as amplitudes do arquivo WAV, separando elas entre os diferentes canais
     * e retornando apenas as amplitudes do canal parametrizado.
     * Extrai apenas as amplitudes entre os pedaços inicial e final.
     * 
     * @param initialChunk - Pedaço inicial (valor da amostra 'sample' baseado no tempo do áudio)
     * @param finalChunk   - Pedaço final (valor da amostra 'sample' baseado no tempo do áudio)
     * 
     * @return amplitudes[]
     */
    public double[] getAmplitudesChunk(int initialChunk, int finalChunk) {
    	return getAmplitudesChunk(1, initialChunk, finalChunk);
    }

    /**
     * Retorna as amplitudes do arquivo WAV, separando elas entre os diferentes canais
     * e retornando apenas as amplitudes do canal parametrizado.
     * Extrai apenas as amplitudes entre os pedaços inicial e final.
     * 
     * @param channel      - Canal do áudio
     * @param initialChunk - Pedaço inicial (valor da amostra 'sample' baseado no tempo do áudio)
     * @param finalChunk   - Pedaço final (valor da amostra 'sample' baseado no tempo do áudio)
     * 
     * @return amplitudes[]
     */
    private double[] getAmplitudesChunk(int channel, int initialChunk, int finalChunk) {
    	int initialChunkData = getChunkDataPosition(initialChunk);
        int finalChunkData = getChunkDataPosition(finalChunk);
        
        int pointerAmplitudeChunk = initialChunkData - wavDataFinalPosition;
        
    	// Se 'finalChunkData' for maior que 'wavDataFinalPosition' e menor que 'wavDataSize',
    	// temos que extrair novamente os dados do arquivo WAV utilizando o método 'extractWavDataChunk' a partir do 'initialChunk'
        if (finalChunkData > wavDataFinalPosition && finalChunkData < wavDataSize) {
        	extractWavDataChunk(initialChunk);
        }

        int amplitude = 0;
        int arrayIndex = 0;
        
        int chunkSize = finalChunk - initialChunk + 1;
        int numChannels = audioWavHeader.getChannels();
        
        double[] amplitudes = new double[chunkSize];

        for (int indexChunk = initialChunk; indexChunk <= finalChunk; indexChunk++) {
        	for (int indexChannel = 1; indexChannel <= numChannels; indexChannel++) {
        		// Apenas o canal parametrizado é processado
        		if (channel == indexChannel) {
        			try {
		        		// 8 bits
		        		if (audioWavHeader.getBitsPerSample() == 8) {
							amplitude = (short) (wavData[pointerAmplitudeChunk] & 0xff);
							amplitude = amplitude - 128;
		        		
		        		// 16 bits
		        		} else if (audioWavHeader.getBitsPerSample() == 16) {
							amplitude = (short) ( wavData[pointerAmplitudeChunk + 0] & 0xff)
									  | (short) ((wavData[pointerAmplitudeChunk + 1] & 0xff) << 8);
		        		
		        		// 24 bits
		        		} else if (audioWavHeader.getBitsPerSample() == 24) {
							amplitude = (int) ( wavData[pointerAmplitudeChunk + 0] & 0xff)
									  | (int) ((wavData[pointerAmplitudeChunk + 1] & 0xff) << 8)
									  | (int) ((wavData[pointerAmplitudeChunk + 2]) << 16);
		        			
		        		// 32 bits
		        		} else if (audioWavHeader.getBitsPerSample() == 32) {
							amplitude = (int) ( wavData[pointerAmplitudeChunk + 0] & 0xff)
									  | (int) ((wavData[pointerAmplitudeChunk + 1] & 0xff) << 8)
									  | (int) ((wavData[pointerAmplitudeChunk + 2] & 0xff) << 16)
									  | (int) ((wavData[pointerAmplitudeChunk + 3]) << 24);
		        			
		        			// 32 bits - IEEE Float (0.24 Float Type 3)
		        			if (audioWavHeader.getAudioFormat() == AudioWavFormat.WAVE_FORMAT_IEEE_FLOAT) {
		        				float fltAmplitude = Float.intBitsToFloat(amplitude);
								amplitude = (int) (fltAmplitude * 2147483647F);
		        			}
		        		}
		        		
		        		if (amplitude == 0) {
							amplitude = 1;   // Atribui valor mínimo à amplitude
		        		}

		        	// Exceção chamada quando 'pointerAmplitudeChunk' exceder a posição máxima do array 'wavData'
        			} catch (Exception e) {
						amplitude = 1;       // Atribui valores mínimos às amplitudes restantes
						isEndOfFile = true;
        			}

        			amplitudes[arrayIndex] = amplitude;
        		}

				pointerAmplitudeChunk += audioWavHeader.getBytesPerSample();
        	}

			arrayIndex++;
        }

        return amplitudes;
    }
    
    /**
     * Retorna a amostra baseando-se no tempo do áudio passado com parâmetro.
     * 
     * @param timeMilliseconds - Tempo em milisegundos
     * 
     * @return sampleFromTime
     */
    public int getSampleFromTime(int timeMilliseconds) {
    	return (int) (((float) getNumSamplesPerChannel() / (float) getTotalTimeInMilliseconds()) * timeMilliseconds);
    }

}