// 紙面照合: 2.20
package org.example.cqrsesbooksamples.chapter02._2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface UserDataModelRepository extends JpaRepository<UserDataModel, String>, JpaSpecificationExecutor<UserDataModel> {
    // 基本的なCRUD操作はJpaRepositoryから継承される
    // 必要に応じてカスタムクエリメソッドを追加できる
}
