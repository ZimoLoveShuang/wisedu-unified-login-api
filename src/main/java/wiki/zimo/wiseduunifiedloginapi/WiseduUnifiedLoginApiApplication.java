package wiki.zimo.wiseduunifiedloginapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class WiseduUnifiedLoginApiApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(WiseduUnifiedLoginApiApplication.class, args);
    }

    /**
     * @auther: 子墨
     * @date: 2018/11/24 19:24
     * @describe: Tomcat启动Springboot项目
     * @param: [builder]
     * @return: org.springframework.boot.builder.SpringApplicationBuilder
     * @version v1.0
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }
}
