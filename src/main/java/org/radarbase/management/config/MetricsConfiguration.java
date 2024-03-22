//package org.radarbase.management.config;
//
//import com.codahale.metrics.JmxReporter;
//import com.codahale.metrics.MetricRegistry;
//import com.codahale.metrics.Slf4jReporter;
//import com.codahale.metrics.health.HealthCheckRegistry;
//import com.codahale.metrics.jvm.BufferPoolMetricSet;
//import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
//import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
//import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
//import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
//import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
//import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
//import com.zaxxer.hikari.HikariDataSource;
//import tech.jhipster.config.JHipsterProperties;
//import java.lang.management.ManagementFactory;
//import java.util.concurrent.TimeUnit;
//import javax.annotation.PostConstruct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@EnableMetrics(proxyTargetClass = true)
//public class MetricsConfiguration extends MetricsConfigurerAdapter {
//
//    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
//    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";
//    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
//    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";
//    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";
//    private static final Logger log = LoggerFactory.getLogger(MetricsConfiguration.class);
//
//    private final MetricRegistry metricRegistry = new MetricRegistry();
//
//    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
//
//    @Autowired
//    private JHipsterProperties jHipsterProperties;
//
//    @Autowired(required = false)
//    private HikariDataSource hikariDataSource;
//
//    @Override
//    public MetricRegistry getMetricRegistry() {
//        return metricRegistry;
//    }
//
//    @Override
//    public HealthCheckRegistry getHealthCheckRegistry() {
//        return healthCheckRegistry;
//    }
//
//    /**
//     * Initialize the metric registry.
//     */
//    @PostConstruct
//    public void init() {
//        log.debug("Registering JVM gauges");
//        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
//        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
//        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
//        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
//        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS,
//                new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
//        if (hikariDataSource != null) {
//            log.debug("Monitoring the datasource");
//            hikariDataSource.setMetricRegistry(metricRegistry);
//        }
//        if (jHipsterProperties.getMetrics().getJmx().isEnabled()) {
//            log.debug("Initializing Metrics JMX reporting");
//            JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
//            jmxReporter.start();
//        }
//        if (jHipsterProperties.getMetrics().getLogs().isEnabled()) {
//            log.info("Initializing Metrics Log reporting");
//            final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
//                    .outputTo(LoggerFactory.getLogger("metrics"))
//                    .convertRatesTo(TimeUnit.SECONDS)
//                    .convertDurationsTo(TimeUnit.MILLISECONDS)
//                    .build();
//            reporter.start(jHipsterProperties.getMetrics().getLogs().getReportFrequency(),
//                    TimeUnit.SECONDS);
//        }
//    }
//}
