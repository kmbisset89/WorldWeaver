package io.github.kmbisset89.worldweaver.domain

/**
 * Builds Govee LAN UDP JSON payloads.
 */
internal class GoveeLanMessageFactory {
    fun scan(): String {
        return """{"msg":{"cmd":"scan","data":{"account_topic":"reserve"}}}"""
    }

    fun turn(powerOn: Boolean): String {
        val value = if (powerOn) 1 else 0
        return """{"msg":{"cmd":"turn","data":{"value":$value}}}"""
    }

    fun brightness(value: Int): String {
        return """{"msg":{"cmd":"brightness","data":{"value":$value}}}"""
    }

    fun color(red: Int, green: Int, blue: Int): String {
        return """{"msg":{"cmd":"colorwc","data":{"color":{"r":$red,"g":$green,"b":$blue},"colorTemInKelvin":0}}}"""
    }
}
