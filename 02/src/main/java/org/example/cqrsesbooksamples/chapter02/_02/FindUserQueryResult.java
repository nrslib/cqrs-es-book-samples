// 紙面照合: 2.21
package org.example.cqrsesbooksamples.chapter02._2;

import java.util.Optional;


// クエリ結果
public record FindUserQueryResult(Optional<UserDataModel> user) {}
