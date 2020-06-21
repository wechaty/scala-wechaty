package wechaty.puppet.schemas

import java.io.{File, FileFilter, PrintWriter}

import scala.io.Source

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-05
  */
object SchemaGenerator {
  val fileNameReg="(\\w+)\\-*(\\w*)\\.ts".r
  val classReg = "(export)\\s+(interface)\\s+(\\w+)\\s*\\{".r
  val typeReg = "(export)\\s+(type)\\s+(\\w+)\\s*=\\s*([^$]+)".r
  val enumReg = "(export)\\s+(enum)\\s+(\\w+)\\s*\\{".r
  val enumFieldReg= "(\\s*)(\\w+)\\s*=\\s*([^,]+),*([^$]*)".r
  val fieldReg= "(\\s*)(\\w+)\\?*\\s*:\\s*([^,]+),*([^$]*)".r
  val jsTypeReg="(\\w+)([\\s\\|\\w]*)".r
  def main(args: Array[String]): Unit = {
    println(args(0))
    val dir = new File(args(0))
    dir.listFiles(new FileFilter {
      override def accept(pathname: File): Boolean = {
        pathname.isFile && pathname.getName != "puppet.ts"
      }
    }).foreach(f=>{
      println(f.getName)
      val filename = f.getName match{
        case fileNameReg(firstName,secondName) =>
          val prefix=firstName.toCharArray
          prefix(0)=prefix(0).toUpper
          val suffix=secondName.toCharArray
          if(suffix.length > 0)
            suffix(0)=suffix(0).toUpper
          new String(prefix)+new String(suffix)
        case other =>
          throw new IllegalStateException("wrong filename")
      }

      val fileWriter = new PrintWriter(new File("wechaty-puppet-padplus/src/main/scala/wechaty/padplus/schemas/"+filename+".scala"))
      fileWriter.println("package wechaty.padplus.schemas")
//      val fileWriter = new PrintWriter(new File("wechaty-puppet/src/main/scala/wechaty/puppet/schemas/"+filename+".scala"))
//      fileWriter.println("package wechaty.puppet.schemas")

      fileWriter.print("object ")
      fileWriter.print(filename)
      fileWriter.println(" {")

      Source.fromFile(f).getLines().foreach {
        case classReg(_, _, className) =>
          fileWriter.print("class ")
          fileWriter.print(className)
          fileWriter.println(" {")
//          println(className)
        case typeReg(_, _, typeName,func) =>
          fileWriter.print("type ")
          fileWriter.print(typeName)
          fileWriter.print(" = ")
          fileWriter.println(func)
          println(typeName,func)
        case enumReg(_, _, enumName) =>
          fileWriter.print("object ")
          fileWriter.print(enumName)
          fileWriter.println(" extends Enumeration {")
          fileWriter.println(" type Type = Value")
          println(enumName)
        case enumFieldReg(space,fieldName,value,comment) =>
          fileWriter.print(space)
          fileWriter.print("val ")
          fileWriter.print(fieldName)
          fileWriter.print(":Type")
          fileWriter.print(" =  Value(")
          fileWriter.print(value)
          fileWriter.print(")")
          if(comment.length > 0) {
            fileWriter.print(" // ")
            fileWriter.print(comment)
          }
          fileWriter.println()
        case fieldReg(space,name, jsType,comment) =>
          fileWriter.print(space)
          fileWriter.print("var ")
          fileWriter.print(name)
          fileWriter.print(":")
          jsType match{
            case jsTypeReg(firstType,_) =>
              val arr = firstType.toCharArray
              arr(0)=arr(0).toUpper
              fileWriter.print(new String(arr))
            case other=>
              fileWriter.print(other.replaceAll("string\\[\\]","Array[String]"))
          }
          fileWriter.print(" = _")
          if(comment.length > 0) {
            fileWriter.print(" // ")
            fileWriter.print(comment)
          }
          fileWriter.println()
          println(name,jsType,comment)
        case other =>
          fileWriter.println(other)
      }

      fileWriter.println(" } ")
      fileWriter.flush()
      fileWriter.close()
    })
  }
}
