package com.johngachihi.parking.web.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
open class Configuration : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.cors(withDefaults()).csrf().disable().authorizeRequests().anyRequest().permitAll()
            .and()
            .formLogin()
            .successHandler { _, _, _ ->  }
    }
}