// 紙面照合: 3.41
// Event Transformationの操作コード
AxonServerConnection connection = AxonServerConnectionFactory
    .forClient("event-transformer")
    .routingServers(new ServerAddress("localhost", 8124))
    .build()
    .connect("default");

Event replacement = Event.newBuilder()
    // 置き換え後のイベント内容を組み立てる
    .build();

connection.eventTransformationChannel()
    .newTransformation("個人情報の修正")               // Transformationの開始
    .thenCompose(active -> active.transform(appender -> {
        appender.replaceEvent(562L, replacement);      // トークン562のイベントを置換
        appender.deleteEvent(100L);                    // トークン100のイベントを削除
    }))
    .thenCompose(ActiveTransformation::startApplying)  // 変更の適用
    .join();
