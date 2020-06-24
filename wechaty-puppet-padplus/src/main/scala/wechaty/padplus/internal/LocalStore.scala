package wechaty.padplus.internal

import java.io.File

import com.google.protobuf.ByteString
import org.fusesource.leveldbjni.JniDBFactory._
import org.iq80.leveldb.{DB, Options}
import wechaty.puppet.schemas.Puppet.objectMapper

import scala.reflect.ClassTag


/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
class LocalStore(storePath:String) {
  private var dbOpt :Option[DB] = None
  def start(): Unit ={
    val options = new Options()
    options.createIfMissing(true)
    val db= factory.open(new File(storePath), options)
    dbOpt = Some(db)
  }
  def put(key: ByteString, value: ByteString): Unit = {
    db.put(key.toByteArray,value.toByteArray)
  }
  def put(key: String, value: String): Unit = {
    put(ByteString.copyFromUtf8(key),ByteString.copyFromUtf8(value))
  }
  def get(key:String): Option[ByteString]={
    get(ByteString.copyFromUtf8(key))
  }
  def getObject[T](key:String)(implicit classTag: ClassTag[T]): Option[T]={
    get(key).map(str=>{
      objectMapper.readValue(str.toStringUtf8,classTag.runtimeClass.asInstanceOf[Class[T]])
    })
  }

  def get(key:ByteString): Option[ByteString]={
    val value = db.get(key.toByteArray)
    if(value != null) Some(ByteString.copyFrom(value))
    else None
  }
  def delete(key:ByteString): Unit ={
    db.delete(key.toByteArray)
  }
  private def db={
    dbOpt match{
      case Some(d) => d
      case _=> throw new IllegalStateException("db is null,database not init?")
    }
  }
  def close(): Unit ={
    db.close()
  }
}
