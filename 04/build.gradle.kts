// 検証補助: 4.14
dependencies {
    implementation("io.vavr:vavr:0.10.6")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    // 〜4.16のSQLを実PostgreSQLで検証するためのテスト専用依存
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.postgresql:postgresql")
}
