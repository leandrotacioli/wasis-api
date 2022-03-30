package br.unicamp.fnjv.wasis.api.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public Path getFileStorageLocation() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

}