package xcoin.core.services

import org.junit.jupiter.api.{Assertions, Test}
import xcoin.core.services.XCoinException.XInvalidParameterException

class XCoinExceptionTest {

  @Test
  def test_message(): Unit = {
    val e = new XInvalidParameterException("name1", 123)
    Assertions.assertEquals("XInvalidParameterException",e.errorName)
    Assertions.assertEquals("invalid value [123] for [name1]",e.getMessage)
    Assertions.assertEquals(1,e.code)
  }
}
