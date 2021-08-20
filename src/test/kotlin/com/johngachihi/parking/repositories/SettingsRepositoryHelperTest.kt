package com.johngachihi.parking.repositories

import com.johngachihi.parking.entities.Config
import com.johngachihi.parking.repositories.settings.JpaSettingsRepositoryHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.persistence.EntityManager

@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
    ]
)
@DisplayName("Test SettingsRepositoryHelper")
class SettingsRepositoryHelperTest {
    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var settingsRepositoryHelper: JpaSettingsRepositoryHelper

    @Test
    fun `When setting is available, getValue() returns it as it is in DB`() {
        entityManager.persist(Config().apply {
            key = "a-unique-key"
            value = "a-config-value"
        })

        val value = settingsRepositoryHelper.getValue("a-unique-key")

        assertThat(value).isEqualTo("a-config-value")
    }

    @Test
    fun `When setting is not set, getValue() returns null`() {
        assertThat(settingsRepositoryHelper.getValue("a-unique-key"))
            .isNull()
    }
}