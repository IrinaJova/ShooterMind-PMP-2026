package com.shootermind.app.domain.model

import java.util.Calendar

enum class ISSFCategory { YOUTH, JUNIOR, SENIOR }

fun calculateISSFCategory(birthDateMs: Long): ISSFCategory {
    val birth = Calendar.getInstance().apply { timeInMillis = birthDateMs }
    val now   = Calendar.getInstance()
    var age   = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
    return when {
        age < 18 -> ISSFCategory.YOUTH
        age < 21 -> ISSFCategory.JUNIOR
        else     -> ISSFCategory.SENIOR
    }
}
