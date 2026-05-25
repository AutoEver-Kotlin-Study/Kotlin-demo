package com.example.haeautoeverstudy.application.adapter.out.persistence

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = [UserJpaRepository::class, MapGroupJpaRepository::class])
class JpaPersistenceAdapterTestConfiguration {
    @Bean
    fun dataSource(): DataSource =
        EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.H2)
            .build()

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan(UserJpaEntity::class.java.packageName)
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setJpaPropertyMap(
                mapOf(
                    "hibernate.hbm2ddl.auto" to "create-drop",
                    "hibernate.show_sql" to "false",
                    "hibernate.format_sql" to "true",
                ),
            )
        }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager(entityManagerFactory)
}
