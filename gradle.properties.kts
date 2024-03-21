rootProject.name=management-portal
profile=dev

kotlin.code.style=official
org.gradle.vfs.watch=true

## below are some of the gradle performance improvement settings that can be used as required, these are not enabled by default

## The Gradle daemon aims to improve the startup and execution time of Gradle.
## The daemon is enabled by default in Gradle 3+ setting this to false will disable this.
## TODO: disable daemon on CI, since builds should be clean and reliable on servers
## https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:ways_to_disable_gradle_daemon
## un comment the below line to disable the daemon

#org.gradle.daemon=false

## Specifies the JVM arguments used for the daemon process.
## The setting is particularly useful for tweaking memory settings.
## Default value: -Xmx1024m -XX:MaxPermSize=256m
## un comment the below line to override the daemon defaults

org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

## When configured, Gradle will run in incubating parallel mode.
## This option should only be used with decoupled projects. More details, visit
## http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:decoupled_projects
## un comment the below line to enable parallel mode

org.gradle.parallel=true

## Enables new incubating mode that makes Gradle selective when configuring projects.
## Only relevant projects are configured which results in faster builds for large multi-projects.
## http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:configuration_on_demand
## un comment the below line to enable the selective mode

#org.gradle.configureondemand=true
