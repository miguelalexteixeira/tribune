package com.sksamuel.tribune.core.strings

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.map

/**
 * Modifies a String -> String [Parser] by uppercasing the output string.
 *
 * @return the output of the underlying parser with the output uppercased.
 */
fun <I, E> Parser<I, String, E>.toUppercase(): Parser<I, String, E> = map { it.uppercase() }

/**
 * Modifies a String -> String [Parser] by lowercasing the output string.
 *
 * @return the output of the underlying parser with the output lowercased.
 */
fun <I, E> Parser<I, String, E>.toLowercase(): Parser<I, String, E> = map { it.lowercase() }
