package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.config.FileStorageConfig;
import br.unicamp.fnjv.wasis.api.dtos.AudioDTO;
import br.unicamp.fnjv.wasis.api.utils.transformations.ClockTransformations;
import br.unicamp.fnjv.wasis.api.utils.crypto.SHA256;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wav.AudioWav;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wav.AudioWavFormat;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wav.AudioWavHeader;
import br.unicamp.fnjv.wasis.api.utils.multimidia.wrapper.FfmpegEncoder;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class AudioService {

    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Autowired
    private FileStorageService fileStorageService;

    public AudioDTO loadAudio(MultipartFile file) {
        try {
            AudioWavHeader header = new AudioWavHeader(file.getInputStream());
            String filePathNameHashed = "";

            // Verifica se o Header do arquivo é válido
            if (header.loadHeader()) {

                // Verifica se o arquivo WAV possui a configuração padrão utilizado pelo WASIS
                if (header.getSampleRate() == AudioWavFormat.TARGET_SAMPLE_RATE && header.getBitsPerSample() == AudioWavFormat.TARGET_BIT_RATE) {
                    filePathNameHashed = fileStorageService.storeFile(file);
                } else {
                    filePathNameHashed = convertAudioFileToDefaultWav(file);
                }

            } else {
                filePathNameHashed = convertAudioFileToDefaultWav(file);
            }

            AudioWav audioWav = new AudioWav(filePathNameHashed);
            audioWav.loadAudio();

            AudioDTO audioDTO = new AudioDTO();
            audioDTO.setHash(audioWav.getAudioFileHash());
            audioDTO.setFileName(audioWav.getAudioFilePath());
            audioDTO.setFileNameOriginal(StringUtils.cleanPath(file.getOriginalFilename()));
            audioDTO.setDurationMilliseconds(audioWav.getTotalTimeInMilliseconds());
            audioDTO.setDurationDigitalFormat(ClockTransformations.millisecondsIntoDigitalFormat(audioWav.getTotalTimeInMilliseconds()));

            audioWav.closeAudio();

            return audioDTO;

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
     *
     * @return filePathNameTargetHashed
     */
    public String convertAudioFileToDefaultWav(MultipartFile file) {
        try {
            String filePathNameSource = fileStorageService.storeFile(file);
            String filePathNameTarget = fileStorageConfig.getFileStorageLocation() + File.separator + RandomStringUtils.randomAlphanumeric(10) + ".wav";

            File fileSource = new File(filePathNameSource);
            File fileTarget = new File(filePathNameTarget);

            FfmpegEncoder objFfmpegEncoder = new FfmpegEncoder();
            objFfmpegEncoder.encode(fileSource, fileTarget);

            String hashedFileTarget = SHA256.getHashFromFile(fileTarget);
            String filePathNameHashed = fileStorageConfig.getFileStorageLocation() + File.separator + hashedFileTarget + ".wav";

            try {
                Files.move(Paths.get(filePathNameTarget), Paths.get(filePathNameHashed));
            } catch (FileAlreadyExistsException e) {
                System.out.println("Não foi possível renomear o arquivo - Hash: " + hashedFileTarget + " - Arquivo já existente.");
            }

            try {
                Files.deleteIfExists(Paths.get(filePathNameTarget));
            } catch (Exception e) {
                System.out.println("Não foi possível excluir o arquivo - : " + filePathNameTarget);
            }

            return filePathNameHashed;

        } catch (Exception e) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao converter arquivo de áudio para o formato padrão WAV do WASIS.", e.getMessage());
        }
    }
}