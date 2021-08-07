package com.johngachihi.parking.repositories.config

import com.johngachihi.parking.entities.Config
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

interface ConfigRepositoryHelper {
    fun getValue(key: String): String?
}

@Repository
interface JpaConfigRepositoryHelper : ConfigRepositoryHelper, JpaRepository<Config, Long> {
    @Query("select c.value from Config c where c.key = :key")
    override fun getValue(@Param("key") key: String): String?
}