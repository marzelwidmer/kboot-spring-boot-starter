import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.4.1" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    id ("net.researchgate.release") version "2.8.1"
    id("idea")
    kotlin("jvm") version "1.4.21" apply false
    kotlin("plugin.spring") version "1.4.21" apply false
    `java-library`
    `maven-publish`
}

fun Project.envConfig() = object : kotlin.properties.ReadOnlyProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String? =
        if (extensions.extraProperties.has(property.name)) {
            extensions.extraProperties[property.name] as? String
        } else {
            System.getenv(property.name)
        }
}

release {
    buildTasks = listOf("releaseBuild")
}

tasks.register("releaseBuild") {
    dependsOn(subprojects.map { it.tasks.findByName("build") })
}



subprojects {
    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "maven-publish")
//    apply(plugin = "kotlin")
//    apply(plugin = "java-library")
//    apply(plugin = "org.jetbrains.kotlin.jvm")
//    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
//    apply(plugin = "io.spring.dependency-management")

    group = "ch.keepcalm"

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { url = uri("https://repo.spring.io/milestone") }
    }


    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        val sourcesJar by creating(Jar::class) {
            dependsOn(JavaPlugin.CLASSES_TASK_NAME)
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        val javadocJar by creating(Jar::class) {
            dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
            archiveClassifier.set("javadoc")
            from(javadoc)
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
            archives(jar)
        }
    }

    tasks.withType<Javadoc>() {
        if (JavaVersion.current().isJava8Compatible) {
            (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}

tasks.named("afterReleaseBuild") {
    dependsOn(listOf(
        ":kboot-spring-boot-starter-autoconfigure:publish",
        ":kboot-spring-boot-starter-autoconfigure-starter:publish")
    )
}
