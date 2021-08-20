package com.johngachihi.parking.repositories.settings

class IllegalFormatException(msg: String) : IllegalArgumentException(msg)

class IllegalSettingException(settingName: String, cause: Throwable) :
    Exception(settingName, cause)