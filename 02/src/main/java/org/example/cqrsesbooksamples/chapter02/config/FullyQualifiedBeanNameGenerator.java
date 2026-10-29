package org.example.cqrsesbooksamples.chapter02.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * 同名クラスが複数パッケージに存在しても Bean 名が衝突しないよう、
 * 完全修飾クラス名を Bean 名として使う。
 */
public class FullyQualifiedBeanNameGenerator extends AnnotationBeanNameGenerator {
    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        return definition.getBeanClassName();
    }
}
