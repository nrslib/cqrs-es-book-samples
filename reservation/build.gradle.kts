plugins {
    java
}

dependencies {
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.testcontainers:junit-jupiter")
}
