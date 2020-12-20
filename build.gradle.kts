import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    id("org.springframework.boot") version "2.4.1" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    id("net.researchgate.release") version "2.8.1"
    id("idea")
    kotlin("jvm") version "1.4.21" apply false
    kotlin("plugin.spring") version "1.4.21" apply false
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

fun Project.envConfig() = object : kotlin.properties.ReadOnlyProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String? =
        if (extensions.extraProperties.has(property.name)) {
            extensions.extraProperties[property.name] as? String
        } else {
            System.getenv(property.name)
        }
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.+")
    }
}

allprojects {

    group = "ch.keepcalm"
    version = "1.0.1"

//    repositories {
//        jcenter()
//        mavenLocal()
//        mavenCentral()
//        maven { url = uri("https://repo.spring.io/milestone") }
//    }
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { url = uri("https://repo.spring.io/milestone") }
    }

    println("Enabling jFrog Bintray plugin in project ${project.name}...")
    apply(plugin = "com.jfrog.bintray")
    println("Enabling Java plugin in project ${project.name}...")
    apply(plugin = "java")
    println("Enabling maven-publish plugin in project ${project.name}...")
    apply(plugin = "maven-publish")
    println("Enabling Kotlin Spring plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    tasks.withType<KotlinCompile> {
        println("Configuring KotlinCompile  $name in project ${project.name}...")
        kotlinOptions {
            languageVersion = "1.4"
            apiVersion = "1.4"
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
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
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    publishing {
        publications {
            println("--------> ${project.name}")
            create<MavenPublication>("${project.name}") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

                pom {
                    name.set("Spring Boot Starter")
                    description.set("A Spring Boot Starter.")
                    url.set("https://github.com/marzelwidmer?tab=repositories")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("marzelwidmer")
                            name.set("Marcel Widmer")
                            email.set("marzelwidmer@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com:marzelwidmer/${project.name}.git")
                        developerConnection.set("https://github.com/marzelwidmer/${project.name}.git")
                        url.set("http://blog.marcelwidmer.org")
                    }
                }
            }
        }
    }

    bintray {
        user = if (project.hasProperty("bintrayUser")) project.property("bintrayUser") as String? else System.getenv("BINTRAY_USER")
        key = if (project.hasProperty("bintrayApiKey")) project.property("bintrayApiKey") as String? else System.getenv("BINTRAY_API_KEY")
        publish = true
        setPublications(project.name)

        pkg.apply {
            //   https://dl.bintray.com/marzelwidmer/myrepository
            repo = "myrepository" //'<repository name on bintray>'
            name = "${project.name}" //'<artifactId>'
            version.apply {
                name = "${project.version}"
                released = org.apache.tools.ant.types.resources.selectors.Date().toString()
                vcsTag = "v${project.version}"
            }
        }
    }
}


tasks.named("afterReleaseBuild") {
    dependsOn(
        listOf(
            ":kboot-spring-boot-starter-autoconfigure:publish",
            ":kboot-spring-boot-starter-autoconfigure-starter:publish"
        )
    )
}


//val artifactName = project.name
//val artifactGroup = project.group.toString()
//val artifactVersion = project.version.toString()
//
//val pomUrl = "https://github.com/marzelwidmer/kboot-spring-boot-starter"
//val pomScmUrl = "https://github.com/marzelwidmer/kboot-spring-boot-starter"
//val pomIssueUrl = "https://github.com/marzelwidmer/kboot-spring-boot-starter/issues"
//val pomDesc = "https://github.com/marzelwidmer/kboot-spring-boot-starter"
//
//val githubRepo = "marzelwidmer/kboot-spring-boot-starter"
//val githubReadme = "README.md"
//
//val pomLicenseName = "MIT"
//val pomLicenseUrl = "https://opensource.org/licenses/mit-license.php"
//val pomLicenseDist = "repo"
//val pomDeveloperId = "marzelwidmer"
//val pomDeveloperName = "Marcel Widmer"
//
//bintray {
//    user = if (project.hasProperty("bintrayUser")) project.property("bintrayUser") as String? else System.getenv("BINTRAY_USER")
//    key = if (project.hasProperty("bintrayApiKey")) project.property("bintrayApiKey") as String? else System.getenv("BINTRAY_API_KEY")
//    publish = true
//    setPublications(project.name)
//
//    pkg.apply {
//        userOrg = "marzelwidmer"
//        repo = "myrepository"
//        name = artifactName
//
//
//        githubRepo = githubRepo
//
//        vcsUrl = pomScmUrl
//        description = "Spring Boot Starter Project written in kotlin"
//
//        setLabels("kotlin", "spring", "springboot")
//        setLicenses("MIT")
//
//        desc = description
//        websiteUrl = pomUrl
//        issueTrackerUrl = pomIssueUrl
//        githubReleaseNotesFile = githubReadme
//
//        version.apply {
//            name = artifactVersion
//            desc = pomDesc
//            released = org.apache.tools.ant.types.resources.selectors.Date().toString()
//            vcsTag = artifactVersion
//        }
//    }
//}
