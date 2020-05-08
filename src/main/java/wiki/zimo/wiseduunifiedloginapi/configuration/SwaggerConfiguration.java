package wiki.zimo.wiseduunifiedloginapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Value("${author.name}")
    private String authorName;
    @Value("${author.url}")
    private String authorUrl;
    @Value("${author.email}")
    private String authorEmail;
    @Value("${api-info.title}")
    private String apiInfoTitle;
    @Value("${api-info.version}")
    private String apiInfoVersion;
    @Value("${api-info.description}")
    private String apiInfoDescription;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("wiki.zimo.wiseduunifiedloginapi"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(apiInfoTitle)
                .contact(new Contact(authorName, authorUrl, authorEmail))
                .version(apiInfoVersion)
                .description(apiInfoDescription)
                .build();
    }
}
