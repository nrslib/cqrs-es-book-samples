# 削除済み（旧 3.60） DLQ再処理とセグメント操作の呼び出し方の違い
# DLQ再処理は1回の呼び出しでOK（ロードバランサー経由）
curl -X POST http://load-balancer/admin/dead-letters/user-projection/process-all

# セグメント分割は所有インスタンスに当たるまでブロードキャスト
for i in {1..10}; do
  curl -X POST http://load-balancer/admin/segments/user-projection/split/0
done

# リビルドは「全台停止 → 1台でリセット → 全台再開」の順
for host in node1 node2 node3; do
  curl -X POST http://$host/admin/segments/user-projection/stop
done
curl -X POST http://node1/admin/segments/user-projection/reset
for host in node1 node2 node3; do
  curl -X POST http://$host/admin/segments/user-projection/start
done
