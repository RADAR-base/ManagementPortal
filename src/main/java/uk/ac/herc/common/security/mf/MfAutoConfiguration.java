package uk.ac.herc.common.security.mf;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "mf.enable", havingValue = "true")
@Import(MfProperties.class)
@AutoConfigurationPackage
@ComponentScan
public class MfAutoConfiguration {

    @Bean
    MfController mfController() {
        return new MfController();
    }

    @Bean
    MfService mfService() {
        return new MfServiceImpl();
    }

}
