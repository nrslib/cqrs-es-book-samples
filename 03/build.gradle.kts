// 検証補助: 3.15, 3.22
// snippets のリスト（orm.xml など）をテストから直接読み込んで検証する
sourceSets {
    named("test") {
        resources.srcDir("snippets")
    }
}

dependencies {
    // リスト3.15（Axon Serverのロードバランシング）とリスト3.22〜3.25（PostgreSQL BYTEA）を
    // 実機で検証するためのテスト専用依存。実行には Docker が必要（なければ自動スキップ）
    testImplementation("org.axonframework:axon-server-connector:4.13.1")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.postgresql:postgresql")
}
