#!/bin/bash
# 削除済み（旧 3.29） 所有情報の収集と分割を行う運用スクリプト
# ステップ1: すべてのインスタンスから所有情報を収集
for i in {1..5}; do
  curl http://load-balancer/admin/segments/user-projection/ownership >> ownership.json
  sleep 1
done

# ステップ2: ownership.jsonから分割が必要なセグメントを判断
# （負荷の偏り、処理の遅れ、エラー状態を確認する）

# ステップ3: ブロードキャストパターンで分割を実行
for i in {1..5}; do
  curl -X POST http://load-balancer/admin/segments/user-projection/split/0
  sleep 1
done

# ステップ4: 所有情報を再収集して分割の反映を確認
