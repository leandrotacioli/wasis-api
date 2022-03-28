package br.unicamp.fnjv.wasis.api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@JsonIgnoreProperties({"$$beanFactory"})
public class WasisInfoDTO {

    @Value("${application.description}")
    private String applicationDescription;

    @Value("${application.version}")
    private String applicationVersion;

    @Value("${project.acronym}")
    private String projectAcronym;

    @Value("${project.name}")
    private String projectName;

    @Value("${project.description}")
    private String projectDescription;

    @Value("${project.developer}")
    private String projectDeveloper;

    @Value("${project.developer.url}")
    private String projectDeveloperUrl;

    @Value("${project.license}")
    private String projectLicense;

    @Value("${project.license.url}")
    private String projectLicenseUrl;

}