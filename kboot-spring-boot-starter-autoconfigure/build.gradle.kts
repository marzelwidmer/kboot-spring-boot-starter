plugins {
    kotlin("jvm")
}

val springBootVersion: String by extra
val springCloudVersion: String by extra

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.12.0'")

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("io.jsonwebtoken", "jjwt", "0.9.1")
    api("com.fasterxml.jackson.module", "jackson-module-afterburner", "2.12.0")

    api("org.zalando", "problem-spring-web", "0.23.0")

    // import Spring Cloud  BOM
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))
    // Spring Cloud dependencies
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")

    // Annotation Processors
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
}


