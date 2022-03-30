package br.unicamp.fnjv.wasis.api.utils.multimidia.wav;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Carrega as especificações do Header de um arquivo WAV.
 * 
 * @author Leandro Tacioli
 */
public class AudioWavHeader {

    private InputStream inputStream;

    private String headerError;
	
    private final int HEADER_BYTE_LENGTH = 65536;
    private byte[] headerBuffer = new byte[HEADER_BYTE_LENGTH];

    private final String RIFF_HEADER = "RIFF";
    private final String WAVE_HEADER = "WAVE";
    
    private final int DEFAULT_FMT_SIZE = 16;   // Tamanho padrão (em bytes) do pedaço 'FMT ' do Header

    private String chunkId;       // 4 bytes, big endian
    private int chunkSize;        // 4 bytes, little endian
    private String format;        // 4 bytes, big endian
    private String subChunk1Id;   // 4 bytes, big endian
    private int subChunk1Size;    // 4 bytes, little endian
    private int audioFormat;      // 2 bytes, little endian
    private int channels;         // 2 bytes, little endian
    private int sampleRate;       // 4 bytes, little endian
    private int byteRate;         // 4 bytes, little endian
    private int blockAlign;       // 2 bytes, little endian
    private int bitsPerSample;    // 2 bytes, little endian
    private int bytesPerSample;
	private String extraParam;    // N bytes, little endian
    private String subChunk2Id;   // 4 bytes, big endian
    private int subChunk2Size;    // 4 bytes, little endian
    
    private int pointer = 0;

    /**
     * Carrega as especificações do Header de um arquivo WAV.
     *
     * @param inputStream
     */
    public AudioWavHeader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Carrega as especificações do Header de um arquivo WAV.
     * 
     * @param audioFilePath
     */
    public AudioWavHeader(String audioFilePath) throws Exception {
        try {
            this.inputStream = new FileInputStream(audioFilePath);
        } catch (Exception e) {
            throw new Exception("Não foi possível carregar o Header do arquivo informado.");
        }
    }

    /**
     * Carrega os dados do Header.
     *
     * @return TRUE - Header válido
     * @throws IOException
     */
    public boolean loadHeader() throws Exception {
        try {
            inputStream.read(headerBuffer);

            headerError = "";

            // Chunk ID (4 bytes - Big endian)
            chunkId = new String(new byte[] { headerBuffer[pointer++],
                                              headerBuffer[pointer++],
                                              headerBuffer[pointer++],
                                              headerBuffer[pointer++] });
            
            if (!chunkId.equals(RIFF_HEADER)) {
                throw new Exception("Header inválido - não é um arquivo WAV.");
            }
            
            // Chunk Size (4 bytes - Little endian)
            chunkSize = (int) (headerBuffer[pointer++] & 0xff)
                      | (int) (headerBuffer[pointer++] & 0xff) << 8
                      | (int) (headerBuffer[pointer++] & 0xff) << 16
                      | (int) (headerBuffer[pointer++] & 0xff) << 24;
            
            // Format (4 bytes - Big endian)
            format = new String(new byte[] { headerBuffer[pointer++],
                                             headerBuffer[pointer++],
                                             headerBuffer[pointer++],
                                             headerBuffer[pointer++] });
            
            if (!format.equals(WAVE_HEADER)) {
                throw new Exception("Header inválido - não é um arquivo WAV.");
            }
            
            // Subchunk 1 ID (4 bytes - Big endian)
            subChunk1Id = new String(new byte[] { headerBuffer[pointer++],
                                                  headerBuffer[pointer++],
                                                  headerBuffer[pointer++],
                                                  headerBuffer[pointer++] });
            
            // Subchunk 1 Size (4 bytes - Little endian)
            subChunk1Size = (int) (headerBuffer[pointer++] & 0xff)
                          | (int) (headerBuffer[pointer++] & 0xff) << 8
                          | (int) (headerBuffer[pointer++] & 0xff) << 16
                          | (int) (headerBuffer[pointer++] & 0xff) << 24;
            
            // Subchunk 1 deve ser 'FMT '
            if (subChunk1Id.toUpperCase().equals("FMT ") == false) {
	            while (subChunk1Id.toUpperCase().equals("FMT ") == false) {
	            	ignoreWavChunk(subChunk1Size);
	            	
	            	// Subchunk 1 ID (4 bytes - Big endian)
	            	subChunk1Id = new String(new byte[] { headerBuffer[pointer++],
                                                          headerBuffer[pointer++],
                                                          headerBuffer[pointer++],
                                                          headerBuffer[pointer++] });
	
	            	// Subchunk 1 Size (4 bytes - Little endian)
	            	subChunk1Size = (int) (headerBuffer[pointer++] & 0xff)
                                  | (int) (headerBuffer[pointer++] & 0xff) << 8
                                  | (int) (headerBuffer[pointer++] & 0xff) << 16
                                  | (int) (headerBuffer[pointer++] & 0xff) << 24;
	            }
            }
            
            // Audio Format (2 bytes - Little endian)
            audioFormat = (int) ((headerBuffer[pointer++] & 0xff)
                               | (headerBuffer[pointer++] & 0xff) << 8);
            
            if (audioFormat != AudioWavFormat.WAVE_FORMAT_PCM) {
                throw new Exception("Header inválido - formato de áudio inválido para os padrões do WASIS.");
            }
            
            // Number of channels (2 bytes - Little endian)
            channels = (int) ((headerBuffer[pointer++] & 0xff)
                            | (headerBuffer[pointer++] & 0xff) << 8);
            
            // Sample Rate (4 bytes - Little endian)
            sampleRate = (int) (headerBuffer[pointer++] & 0xff)
                       | (int) (headerBuffer[pointer++] & 0xff) << 8
                       | (int) (headerBuffer[pointer++] & 0xff) << 16
                       | (int) (headerBuffer[pointer++] & 0xff) << 24;
            
            // Byte Rate (4 bytes - Little endian)
            byteRate = (int) (headerBuffer[pointer++] & 0xff)
                     | (int) (headerBuffer[pointer++] & 0xff) << 8
                     | (int) (headerBuffer[pointer++] & 0xff) << 16
                     | (int) (headerBuffer[pointer++] & 0xff) << 24;
            
            // Block Align (2 bytes - Little endian)
            blockAlign = (int) ((headerBuffer[pointer++] & 0xff)
                              | (headerBuffer[pointer++] & 0xff) << 8);
            
            // Bits per Sample (2 bytes - Little endian)
            bitsPerSample = (int) ((headerBuffer[pointer++] & 0xff)
                                 | (headerBuffer[pointer++] & 0xff) << 8);
            
            bytesPerSample = bitsPerSample / 8;
            
            // Informações extras caso 'intSubChunk1Size' for maior que 'DEFAULT_FMT_SIZE'
            extraParam = "";
            if (subChunk1Size > DEFAULT_FMT_SIZE) {
            	extraParam = new String(getDynamicByteArray(subChunk1Size - DEFAULT_FMT_SIZE));
            }
            
            // Subchunk 2 ID (4 bytes - Big endian)
            subChunk2Id = new String(new byte[] { headerBuffer[pointer++],
                                                  headerBuffer[pointer++],
                                                  headerBuffer[pointer++],
                                                  headerBuffer[pointer++] });
            
            // Subchunk 2 Size (4 bytes - Little endian)
            subChunk2Size = (int) (headerBuffer[pointer++] & 0xff)
                          | (int) (headerBuffer[pointer++] & 0xff) << 8
                          | (int) (headerBuffer[pointer++] & 0xff) << 16
                          | (int) (headerBuffer[pointer++] & 0xff) << 24;
            
            // Subchunk 2 deve ser 'DATA'
            if (subChunk2Id.toUpperCase().equals("DATA") == false) {
	            while (subChunk2Id.toUpperCase().equals("DATA") == false) {
	            	ignoreWavChunk(subChunk2Size);
	            	
	            	// Subchunk 2 ID (4 bytes - Big endian)
	            	subChunk2Id = new String(new byte[] { headerBuffer[pointer++],
                                                          headerBuffer[pointer++],
                                                          headerBuffer[pointer++],
                                                          headerBuffer[pointer++] });
	
	            	// Subchunk 2 Size (4 bytes - Little endian)
	            	subChunk2Size = (int) (headerBuffer[pointer++] & 0xff)
                                  | (int) (headerBuffer[pointer++] & 0xff) << 8
                                  | (int) (headerBuffer[pointer++] & 0xff) << 16
                                  | (int) (headerBuffer[pointer++] & 0xff) << 24;
	            }
            }
            
        } catch (Exception e) {
            headerError = e.getMessage();

        } finally {
            inputStream.close();
        }

        if (headerError.equals("")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Ignora um pedaço do cabeçalho, sendo que o tamanho do pedaço é passado como parâmetro.
     * É utilizado quando um pedaço do cabeçalho é esperado, mas outro é carregado.
     * 
     * @param chunkSize
     */
    private void ignoreWavChunk(int chunkSize) {
        pointer += chunkSize;
    }
    
    /**
     * Retorna o array de bytes de um comprimento parametrizado.
     * 
     * @param arrayLength
     * 
     * @return array - Big endian format
     */
    private byte[] getDynamicByteArray(int arrayLength) {
    	byte[] array = new byte[arrayLength];
		
		for (int indexArrayLength = 0; indexArrayLength < arrayLength; indexArrayLength++) {
			array[indexArrayLength] = headerBuffer[pointer++];
		}
    	
		return array;
    }

    /**
     * Retorna o formato do áudio.
     *
     * @return headerError
     */
    public String getHeaderError() {
        return headerError;
    }
    
    /**
     * Retorna o formato do áudio.
     * 
     * @return audioFormat
     */
    public int getAudioFormat() {
        return audioFormat;
    }

    /**
     * Retorna o número de canais. <br>
     * <i>1</i> - Mono
	 * <br>
	 * <i>2</i> - Stereo
     * 
     * @return channels
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Retorna a taxa de amostragem. <br>
     * <i>48,000</i> - Padrão do WASIS
     * 
     * @return sampleRate
     */
    public int getSampleRate() {
        return sampleRate;
    }
    
    /**
     * Retorna a taxa de bytes.
     * 
     * @return byteRate
     */
    public int getByteRate() {
        return byteRate;
    }

    /**
     * Retorna o número de bits por amostra.
     * 
     * @return bitsPerSample
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }
    
    /**
     * Retorna o número de bytes por amostra.
     * 
     * @return bytesPerSample
     */
    public int getBytesPerSample() {
        return bytesPerSample;
    }
    
    /**
     * Imprime as especificações do Header do arquivo WAV.
     */
    public void printWavHeader() {
        StringBuffer wavHeaderInfo = new StringBuffer();
        wavHeaderInfo.append("Chunk Id: " + chunkId + "\n");
        wavHeaderInfo.append("Chunk Size: " + chunkSize + "\n");
        wavHeaderInfo.append("Format: " + format + "\n");
        wavHeaderInfo.append("SubChunk 1 ID: " + subChunk1Id + "\n");
        wavHeaderInfo.append("SubChunk 1 Size: " + subChunk1Size + "\n");
        wavHeaderInfo.append("Audio Format: " + audioFormat + "\n");
        wavHeaderInfo.append("Channels: " + channels + "\n");
        wavHeaderInfo.append("Sample Rate: " + sampleRate + "\n");
        wavHeaderInfo.append("Byte Rate: " + byteRate + "\n");
        wavHeaderInfo.append("Block Align: " + blockAlign + "\n");
        wavHeaderInfo.append("Bits Per Sample: " + bitsPerSample + "\n");
        wavHeaderInfo.append("Extra Param: " + extraParam + "\n");
        wavHeaderInfo.append("SubChunk 2 ID: " + subChunk2Id + "\n");
        wavHeaderInfo.append("SubChunk 2 Size: " + subChunk2Size + "\n");
        
        System.out.println(wavHeaderInfo);
    }

}