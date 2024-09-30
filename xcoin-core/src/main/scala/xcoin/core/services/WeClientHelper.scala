package xcoin.core.services

import java.lang.reflect.ParameterizedType
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe.Type

object WeClientHelper {
  def parameterizedType(paramType: Type): java.lang.reflect.Type = {
    val typeConstructor = currentMirror.runtimeClass(paramType)

    val innerTypes = paramType.typeArgs.map(parameterizedType).toArray

    if (innerTypes.isEmpty) {
      typeConstructor
    } else {
      new ParameterizedType {
        override def getRawType: java.lang.reflect.Type = {
          typeConstructor
        }

        override def getActualTypeArguments: Array[java.lang.reflect.Type] = {
          innerTypes
        }

        override def getOwnerType: java.lang.reflect.Type = {
          null
        }
      }
    }
  }

}
