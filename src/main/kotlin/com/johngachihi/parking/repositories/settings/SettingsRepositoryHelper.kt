package com.johngachihi.parking.repositories.settings

import com.johngachihi.parking.entities.Config
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

interface SettingsRepositoryHelper {
    fun getValue(key: String): String?
}

// TODO: Cache results
@Repository
interface JpaSettingsRepositoryHelper : SettingsRepositoryHelper, JpaRepository<Config, Long> {
    @Query("select c.value from Config c where c.key = :key")
    override fun getValue(@Param("key") key: String): String?
}