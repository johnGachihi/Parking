package com.johngachihi.parking.web.parkingTariffSettings

import com.johngachihi.parking.entities.ParkingTariff
import com.johngachihi.parking.services.parkingTariffSettings.isUpperLimitUnique
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [UniqueUpperLimitValidator::class])
annotation class UniqueUpperLimit(
    val message: String = "Upper limits must be unique",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)


class UniqueUpperLimitValidator :
    ConstraintValidator<UniqueUpperLimit, List<ParkingTariff>>
{
    override fun isValid(value: List<ParkingTariff>?, context: ConstraintValidatorContext): Boolean {
        return value?.let { isUpperLimitUnique(it) } ?: true
    }
}