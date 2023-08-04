# common-security
This repository contains two features for security: Two factor(Password and email) authentication and Rate Limiting.
These two features can be enabled separately.

The module should be integrated with project build with **Gradle**
[Migrate your Maven project to Gradle](https://www.baeldung.com/maven-convert-to-gradle)

## Integration Guide

* Add this module to your project:
```
git subtree add --prefix=your-application-dir/security https://github.com/UoM-Digital-Health-Software/common-security.git main --squash
```
* Update settings.gradle, add the line at the end of the file
```
include 'security'
```
* Update build.gradle, add the dependency
```
implementation project(':security')
```


### Enable Two Factor Authentication
#### Add table schema
1. Copy the change set to your liquibase changelog [TwoFactorAuthentication Entity](https://github.com/UoM-Digital-Health-Software/common-security/blob/main/src/main/resources/20210810105214_added_entity_TwoFactorAuthentication.xml)
2. Add the change set to your liquibase master.xml
3. Add Database configuration
   ```
   @EnableJpaRepositories(
    basePackages = {"...", "uk.ac.herc.common.security.mf"})
   @EntityScan(basePackages = {"...", "uk.ac.herc.common.security.mf"})
   @ComponentScan(basePackages = { "uk.ac.herc.dhs.*", "uk.ac.herc.common.security.mf"})
   ```
#### Add configuration in your application.yaml
```
mf:
  enable: true
```

```
spring:
  mail:
    host: mailrouter.man.ac.uk
```

### Implement a [MfOptSender](https://github.com/UoM-Digital-Health-Software/common-security/blob/main/src/main/java/uk/ac/herc/common/security/mf/MfOptSender.java) in you Spring ApplicationContext, 
If you are using JHipster to generate the project, you can easily configure it using the EmailService in your application.
```
@Service("mfOptSender")
public class MfOptSenderImpl implements MfOptSender {
  @Autowired
  private MailService mailService;
  @Override
  public void sendUserOpt(String userName, String otp) {
    mailService.sendEmail(userName, "OTP", otp, false, false);
  }
}
```

#### The project provides you two ways to do the Two Factor Authentication:
- Using Rest API directly

  - ```/api/mf-authenticate/code``` 

    Used to generate the OTP

    Post ```{"userName":"can not be null", "password":"can be null if you don't want to check password"}```
  - ```/api/mf-authenticate``` 

    Used to validate the OTP

    Post ```{"userName":"can not be null", "password":"can not be null", "code":"can not be null", "rememberMe":"can be null, default false"}```
- Using the [MfService](https://github.com/UoM-Digital-Health-Software/common-security/blob/main/src/main/java/uk/ac/herc/common/security/mf/MfService.java)

  - [generateOTP(userName)](https://github.com/UoM-Digital-Health-Software/common-security/blob/main/src/main/java/uk/ac/herc/common/security/mf/MfService.java#L4) is used to generate the OTP

  - [validateOTP(userName, otp)](https://github.com/UoM-Digital-Health-Software/common-security/blob/main/src/main/java/uk/ac/herc/common/security/mf/MfService.java#L6) is used to validate the otp

The APIs should be accessable without authentication, add the configuration to your ```SecurityConfiguration```
```
antMatchers("/api/mf-authenticate").permitAll()
antMatchers("/api/mf-authenticate/code").permitAll()
```

#### Configuration:
  
  ```mf.send-otp```, default true, send otp email to the userName(it should be email address).
  
  ```mf.auth-before-generate-otp``` default false, indicate if checking password before generating the OTP.
  
  ```mf.expire-time``` default 30, unit minute, the OTP expire time
  
  ```mf.max-attempt-times``` default 5, the maximum tries within expire time 

### Enable Rate Limiting
add configuration below in your application.yaml
```
bucket4j:
  enable: true
  filters:
    - cache-name: rate-limit-buckets
      strategy: first
      url: .*
      filter-method: servlet
      http-response-body: "{ \"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"You have exhausted your API Request Quota\" }"
      rate-limits:
        - expression: "getRemoteAddr()"
          execute-condition: "1==1"
          bandwidths:
            - capacity: 100
              time: 1
              unit: minutes
              
```
The configuration will set limit: 100 requests per IP

You can find more configurations from [bucket4j](https://www.baeldung.com/spring-bucket4j)

## Pull latest changes
```
git subtree pull --prefix=you-application-dir/security https://github.com/UoM-Digital-Health-Software/common-security.git main --squash
```

## The application using this project
[iMinds](https://github.com/UoM-Digital-Health-Software/iMinds)

[bay-trial](https://github.com/UoM-Digital-Health-Software/bay-trial)

