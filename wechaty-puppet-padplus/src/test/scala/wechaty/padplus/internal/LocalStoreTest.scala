// Copyright 2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package wechaty.padplus.internal

import org.junit.jupiter.api.{Assertions, Test}


/**
 * Created by jcai on 14-8-17.
 */
class LocalStoreTest {
  @Test
  def test_version() {
    val store = new LocalStore("/tmp/test.local")
    store.start()
    for (v <- 0 until 100) {
      store.put("asdf", "fdsa")
      store.put("sdf", "fdsa")
      store.put("df", "fdsa")
    }
    Assertions.assertEquals("fdsa", store.get("asdf").get.toStringUtf8)
    store.close()
  }
}
