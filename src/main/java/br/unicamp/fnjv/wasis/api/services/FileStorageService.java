package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.config.FileStorageConfig;
import br.unicamp.fnjv.wasis.api.utils.crypto.SHA256;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageLocation = fileStorageConfig.getFileStorageLocation();

        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create the directory where the uploaded files will be stored.", ex.getMessage());
        }
    }

    public String storeFile(MultipartFile file) {
        String storedFileName = "";
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            String fileNameExtension = getFileExtension(file.getOriginalFilename());
            String hashedFileName = SHA256.getHashFromFile(file);
            storedFileName = hashedFileName + ((fileNameExtension != null || !fileNameExtension.equals("")) ? ("." + fileNameExtension) : "");

            // Copy file to the target location (Replacing with the SHA-256 Checksum name)
            Path targetLocation = fileStorageLocation.resolve(storedFileName);

            try {
                Files.copy(file.getInputStream(), targetLocation);
            } catch (FileAlreadyExistsException ex) {
                System.out.println("Could not copy file - Hash: " + hashedFileName + " - File already exists.");
            }

            storedFileName = targetLocation.toFile().getPath();

        } catch (Exception ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file '" + originalFileName + "'. Please try again!", ex.getMessage());
        }

        return storedFileName;
    }

    public Resource loadFileAsResource(String fileName) {
        Resource resource;

        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "File not found " + fileName);
            }

        } catch (MalformedURLException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "File not found " + fileName, ex.getMessage());
        }

        return resource;
    }

    public String getFileExtension(String filename) {
        try {
            Optional<String> fileExtension = Optional.ofNullable(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1));

            return fileExtension.get();
        } catch (Exception e) {
            return "";
        }
    }

}