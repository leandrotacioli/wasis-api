package br.unicamp.fnjv.wasis.api.services;

import br.unicamp.fnjv.wasis.api.config.FileStorageConfig;
import br.unicamp.fnjv.wasis.api.utils.exceptions.GeneralException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            fileName = targetLocation.toFile().getPath();

        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file " + fileName + ". Please try again!", ex.getMessage());
        }

        return fileName;
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

}