package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.config.FileStorageConfig;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wav.AudioWavFormat;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wav.AudioWavHeader;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wrapper.FfmpegEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class AudioService {

    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Autowired
    private FileStorageService fileStorageService;

    public void loadAudio(MultipartFile file) {
        try {
            AudioWavHeader header = new AudioWavHeader(file.getInputStream());

            // Verifica se o Header do arquivo é válido
            if (header.loadHeader()) {

                // Verifica se o arquivo WAV possui a configuração padrão utilizado pelo WASIS
                if (header.getSampleRate() != AudioWavFormat.TARGET_SAMPLE_RATE || header.getBitsPerSample() != AudioWavFormat.TARGET_BIT_RATE) {
                    convertAudioFileToDefaultWav(file);
                }

            } else {
                convertAudioFileToDefaultWav(file);
            }

        } catch (GeneralException e) {
            throw e;

        } catch (Exception e) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, "Erro ao carregar arquivo de áudio.", e.getMessage());
        }
    }

    /**
     * Converte o arquivo para um formato padrão WAV utilizado pelo WASIS.
     *
     * @param file
     */
    public void convertAudioFileToDefaultWav(MultipartFile file) {
        try {
            String filePathNameSource = fileStorageService.storeFile(file);

            File fileSource = new File(filePathNameSource);
            File fileTarget = new File(fileStorageConfig.getFileStorageLocation() + "/" + Math.random() + ".wav");

            FfmpegEncoder objFfmpegEncoder = new FfmpegEncoder();
            objFfmpegEncoder.encode(fileSource, fileTarget);

        } catch (Exception e) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao converter arquivo de áudio para o formato padrão WAV do WASIS.", e.getMessage());
        }
    }
}