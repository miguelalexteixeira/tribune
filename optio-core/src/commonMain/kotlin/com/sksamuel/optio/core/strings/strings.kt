package com.sksamuel.optio.core.strings

import com.sksamuel.optio.core.Parser
import com.sksamuel.optio.core.filter
import com.sksamuel.optio.core.flatMap
import com.sksamuel.optio.core.invalid
import com.sksamuel.optio.core.map
import com.sksamuel.optio.core.valid

fun <E> Parser.Companion.nonBlankString(ifError: () -> E): Parser<String?, String, E> =
   from<String?>().notNullOrBlank(ifError)

/**
 * Modifies the output of a String producing [Parser] by trimming the output string
 * to remove prefix and suffix whitespace.
 *
 * @return the output of the underlying parser with whitespace trimmed.
 */
fun <I, E> Parser<I, String, E>.trim(): Parser<I, String, E> = map { it.trim() }

/**
 * Modifies the output of a String producing [Parser] to strip the given [chars].
 */
fun <I, E> Parser<I, String, E>.strip(chars: CharArray): Parser<I, String, E> =
   map { chars.fold(it) { acc, c -> acc.replace(c.toString(), "") } }

/**
 * Constrains a String producing parser to ensure it matches the given [regex].
 */
fun <I, E> Parser<I, String, E>.match(regex: Regex, ifError: (String) -> E): Parser<I, String, E> =
   filter({ it.matches(regex) }, ifError)

/**
 * Composes an existing String? producing [Parser] to reject nulls as errors.
 * In addition, also rejects blank strings. Error messages are generated by
 * the given function [ifError].
 *
 * @param ifError the error generating function used if the input is null or blank.
 *
 * @return valid if the input string is not null and not blank, otherwise invalid
 */
fun <I, E> Parser<I, String?, E>.notNullOrBlank(ifError: () -> E): Parser<I, String, E> {
   return flatMap { if (it.isNullOrBlank()) ifError().invalid() else it.valid() }
}

/**
 * Wraps an existing String -> String [Parser] to reject blank strings,
 * with the error message generated by the given function [ifBlank].
 *
 * @param ifBlank the error generating function
 *
 * @return invalid if the input string contains only whitespace, otherwise valid
 */
fun <I, E> Parser<I, String, E>.notBlank(ifBlank: () -> E): Parser<I, String, E> {
   return flatMap {
      if (it.isBlank()) ifBlank().invalid() else it.valid()
   }
}

/**
 * Wraps an existing String -> String [Parser] to reject blank strings,
 * with the error message generated by the given function [ifBlank].
 *
 * Nulls are acceptable and passed through.
 *
 * @param ifBlank the error generating function
 *
 * @return invalid if the input string contains only whitespace, otherwise valid
 */
fun <I, E> Parser<I, String?, E>.nullOrNotBlank(ifBlank: () -> E): Parser<I, String?, E> {
   return flatMap {
      if (it == null) null.valid() else if (it.isBlank()) ifBlank().invalid() else it.valid()
   }
}
