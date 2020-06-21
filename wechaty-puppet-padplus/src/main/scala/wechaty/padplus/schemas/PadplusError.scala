package wechaty.padplus.schemas

import wechaty.padplus.schemas.PadplusEnums.PadplusErrorType

object PadplusError {

  class PadplusError(val errorType: PadplusErrorType.Type, message: String) extends Error {
  }

}
