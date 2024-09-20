package org.radarbase.management.domain.support

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Class to inject dependencies into classes that do not support Autowire, such as JPA event
 * listeners.
 */
@Component
class AutowireHelper private constructor() : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

    companion object {
        /**
         * Get the singleton instance.
         *
         * @return the singleton instance.
         */
        val instance = AutowireHelper()
        private var applicationContext: ApplicationContext? = null

        /**
         * Tries to autowire the specified instance of the class if one of the specified beans which
         * need to be autowired are null.
         *
         * @param classToAutowire the instance of the class which holds @Autowire annotations
         * @param beansToAutowireInClass the beans which have the @Autowire annotation in the specified
         * {#classToAutowire}
         */
        fun autowire(
            classToAutowire: Any,
            vararg beansToAutowireInClass: Any?,
        ) {
            for (bean in beansToAutowireInClass) {
                if (bean == null) {
                    applicationContext!!.autowireCapableBeanFactory.autowireBean(classToAutowire)
                    return
                }
            }
        }
    }
}
