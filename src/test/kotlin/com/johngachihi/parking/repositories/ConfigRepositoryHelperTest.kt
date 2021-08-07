package com.johngachihi.parking.repositories

import com.johngachihi.parking.entities.Config
import com.johngachihi.parking.repositories.config.JpaConfigRepositoryHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.persistence.EntityManager

@DataJpaTest
@DisplayName("Test ConfigRepositoryHelper")
class ConfigRepositoryHelperTest {
    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var configRepositoryHelper: JpaConfigRepositoryHelper

    @Test
    fun `When configuration is available, getValue() returns it as it is in DB`() {
        entityManager.persist(Config().apply {
            key = "a-unique-key"
            value = "a-config-value"
        })

        val value = configRepositoryHelper.getValue("a-unique-key")

        assertThat(value).isEqualTo("a-config-value")
    }

    @Test
    fun `When configuration is not set, getValue() returns null`() {
        assertThat(configRepositoryHelper.getValue("a-unique-key"))
            .isNull()
    }
}