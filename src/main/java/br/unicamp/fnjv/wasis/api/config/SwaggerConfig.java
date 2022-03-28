package br.unicamp.fnjv.wasis.api.config;

import br.unicamp.fnjv.wasis.api.dtos.WasisInfoDTO;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Autowired
    private WasisInfoDTO wasisInfoDTO;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(getApiInformation());
    }

    private Info getApiInformation() {
        return new Info()
                .title(wasisInfoDTO.getApplicationDescription())
                .description(wasisInfoDTO.getProjectDescription())
                .version(wasisInfoDTO.getApplicationVersion())
                .contact(new Contact().name(wasisInfoDTO.getProjectDeveloper()).url(wasisInfoDTO.getProjectDeveloperUrl()))
                .license(new License().name(wasisInfoDTO.getProjectLicense()).url(wasisInfoDTO.getProjectLicenseUrl()));
    }

}