@file:JvmName("GlobalFunctions")

package ch.keepcalm.security

import org.springframework.security.core.context.SecurityContextHolder

fun getPrincipal(): Any? = SecurityContextHolder.getContext().authentication?.principal
fun getBearerFromSecurityContext(): String = "Bearer ${SecurityContextHolder.getContext().authentication.credentials}"

