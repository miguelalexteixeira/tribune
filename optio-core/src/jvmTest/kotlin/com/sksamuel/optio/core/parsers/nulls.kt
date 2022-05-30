package com.sksamuel.optio.core.parsers

import com.sksamuel.optio.core.Parser
import com.sksamuel.optio.core.allowNulls
import com.sksamuel.optio.core.valid
import com.sksamuel.optio.core.withDefault
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NullTest : FunSpec() {
   init {

      test("default") {
         val p = Parser<String?>().withDefault { "wibble" }
         p.parse("abc") shouldBe "abc".valid()
         p.parse(null) shouldBe "wibble".valid()
      }

      test("nullable") {
         val p = Parser<String>().allowNulls()
         p.parse("abc") shouldBe "abc".valid()
         p.parse(null) shouldBe null.valid()
      }
   }
}
