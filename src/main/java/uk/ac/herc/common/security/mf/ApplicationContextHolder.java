package uk.ac.herc.common.security.mf;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    /**
     * This method will throw beanNotFound exception, when the there is not a bean configured in the app.
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }

    public static<T> T getBeanIfExist(Class<T> tClass){
        try{
            return applicationContext.getBean(tClass);
        }catch (Exception e){
            //the bean may not configured
            return null;
        }
    }

    public static<T> T getBeanIfExist(String beanName){
        try{
            return (T) applicationContext.getBean(beanName);
        }catch (Exception e){
            //the bean may not configured
            return null;
        }
    }

}
