package com.johngachihi.parking.repositories.settings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.Duration

interface PaymentSettingsRepository {
    val maxAgeBeforePaymentExpiry: Duration
    val maxAgeBeforePaymentSessionExpiry: Duration
}

@Repository
class DefaultPaymentSettingsRepository(
    @Autowired private val settingsRepositoryHelper: SettingsRepositoryHelper,
    @Autowired private val durationSettingParser: DurationSettingParser,
) : PaymentSettingsRepository {
    override val maxAgeBeforePaymentExpiry: Duration
        get() = getDurationSettingValue(
            settingKey = "max_age_before_payment_expiry_in_minutes",
            settingReadableName = "Max Age Before Payment Expiry (minutes)",
            default = Duration.ofMinutes(20)
        )

    // TODO: Tests
    //       Does it need testing given that its twin brother above
    //       has been tested
    override val maxAgeBeforePaymentSessionExpiry: Duration
        get() = getDurationSettingValue(
            settingKey = "max_age_before_payment_session_expiry_in_minutes",
            settingReadableName = "Max Age Before Payment Session Expiry (minutes)",
            default = Duration.ofMinutes(10)
        )

    private fun getDurationSettingValue(
        settingKey: String,
        settingReadableName: String,
        default: Duration
    ): Duration {
        val value = settingsRepositoryHelper.getValue(settingKey)
            ?: return default

        try {
            return durationSettingParser.parseMinutesString(value)
        } catch (e: IllegalFormatException) {
            throw IllegalSettingException(settingReadableName, e)
        } catch (e: NumberFormatException) {
            throw IllegalSettingException(settingReadableName, e)
        }
    }
}

