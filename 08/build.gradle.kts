dependencies {
    implementation(project(":03"))
    testImplementation(project(":06"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
}
