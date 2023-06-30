package com.example.bpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//RestAPI를 한눈에 확인할 수 있는 Swagger를 위해 생성한 자바 빈 Configuration(환경설정) 클래스
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket restAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //본인의 패키명과 일치 시켜야함
                .apis(RequestHandlerSelectors.basePackage("com.example.bpm"))
                //전체 URI를 다 띄워준다.
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //Swagger index.html 들어갔을 때 title
                .title("Spring Boot REST API Swagger Test")
                // 버젼관리를 위한 text
                .version("1.0.0")
                // 설명 함수
                .description("Swagger API Test 입니다.")
                // 실행 함수 (내장 라이브러리에 위치)
                .build();
    }
}
