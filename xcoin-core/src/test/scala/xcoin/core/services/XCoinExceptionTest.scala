package xcoin.core.services

import org.junit.jupiter.api.{Assertions, Test}
import xcoin.core.services.XCoinException.InvalidParameter

class XCoinExceptionTest {

  @Test
  def test_message(): Unit = {
    val e = new XCoinException(InvalidParameter("name1",123))
    Assertions.assertEquals("InvalidParameter",e.errorName)
    Assertions.assertEquals("invalid value [123] for [name1]",e.getMessage)
  }
}
