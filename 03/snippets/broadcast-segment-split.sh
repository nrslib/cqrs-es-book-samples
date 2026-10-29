#!/usr/bin/env bash
# 紙面照合: 3.19
# 分割はべき等ではないため、成功後は同じ操作を送らない
for i in {1..10}; do
  response=$(curl -qfsS --max-time 10 -X POST \
    http://load-balancer/admin/segments/user-projection/split/0) || exit 1
  status=$(printf '%s\n' "$response" | jq -er '.status') || exit 1
  case "$status" in
    SUCCESS)
      printf '%s\n' "$response"
      exit 0
      ;;
    NOT_OWNED) ;;
    *)
      printf '分割操作が失敗しました: %s\n' "$response" >&2
      exit 1
      ;;
  esac
done
printf '10回の試行で所有者への分割操作が成功しませんでした\n' >&2
exit 1
