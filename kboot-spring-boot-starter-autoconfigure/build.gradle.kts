plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
}

val springBootVersion: String by extra
dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.springframework.boot:spring-boot-starter:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

    compileOnly("io.jsonwebtoken", "jjwt", "0.9.1")
    compileOnly("org.springframework.boot:spring-boot-starter-security:$springBootVersion")

//    implementation("javax.xml.bind", "jaxb-api", "2.4.0-b180830.0359")
//    implementation("org.zalando", "problem-spring-web", "0.23.0")
//    implementation("com.fasterxml.jackson.module", "jackson-module-afterburner")
//    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
//    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")


    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
}
