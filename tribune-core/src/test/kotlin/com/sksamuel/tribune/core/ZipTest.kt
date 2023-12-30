package com.sksamuel.tribune.core

import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import com.sksamuel.tribune.core.strings.minlen
import com.sksamuel.tribune.core.strings.notNullOrBlank
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ZipTest : FunSpec() {
   init {

      test("zip 2") {
         val p1 = Parser.from<String?>().notNullOrBlank { "foo" }.minlen(2) { "Must have length > 2" }
         val p2 = Parser.from<String?>().notNullOrBlank { "bar" }
         val p = Parser.zip(p1, p2)
         p.parse(null) shouldBe nonEmptyListOf("foo", "bar").left()
//         p.parse("a") shouldBe nonEmptyListOf("Must have length > 2").left()
//         p.parse("ab") shouldBe Pair("ab", "ab").right()
      }

      xtest("zip 3") {
         val p1 = Parser.from<String?>().notNullOrBlank { "foo" }.minlen(2) { "Must have length > 2" }
         val p2 = Parser.from<String?>().notNullOrBlank { "bar" }
         val p3 = Parser.from<String?>().notNullOrBlank { "baz" }
         val p = Parser.zip(p1, p2, p3)
//         p.parse(null) shouldBe nonEmptyListOf("foo", "bar", "baz").left()
         p.parse("a") shouldBe nonEmptyListOf("Must have length > 2").left()
         p.parse("ab") shouldBe Triple("ab", "ab", "ab").right()
      }
   }
}
