package com.springboot.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Spring Boot Web Configuration สำหรับ JSP + Static Resources
 */
@Configuration
public class WebConfig {

    /**
     * ตั้งค่า ViewResolver สำหรับ JSP
     * - prefix: โฟลเดอร์ JSP
     * - suffix: นามสกุลไฟล์ JSP
     */
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/"); // วาง JSP ไว้ src/main/webapp/WEB-INF/views/
        resolver.setSuffix(".jsp");
        return resolver;
    }

    /**
     * ทำให้การกด Run Java ใน VS Code พบ JSP จาก src/main/webapp ได้โดยไม่ต้อง
     * package WAR ก่อนทุกครั้ง (ยังใช้ WAR document root ตามปกติเมื่อ deploy จริง)
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> developmentDocumentRoot() {
        File sourceWebApp = findSourceWebApp();
        return factory -> {
            if (sourceWebApp != null) {
                factory.setDocumentRoot(sourceWebApp);
            }
        };
    }

    private File findSourceWebApp() {
        return List.of(Path.of("src", "main", "webapp"),
                        Path.of("SkillXSkillByCreditTime", "src", "main", "webapp"))
                .stream()
                .map(Path::toFile)
                .filter(File::isDirectory)
                .findFirst()
                .orElse(null);
    }

}
