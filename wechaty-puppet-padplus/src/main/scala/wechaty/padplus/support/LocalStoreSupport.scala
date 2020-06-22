package wechaty.padplus.support

import com.google.protobuf.ByteString
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.internal.LocalStore

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait LocalStoreSupport {
  self:PuppetPadplus=>
  protected var store:LocalStore =  _
  private val uinKey = ByteString.copyFromUtf8("uin")
  protected def saveUin(uin:ByteString): Unit ={
    if(!uin.isEmpty){
      store.put(uinKey,uin)
    }
  }
  protected def getUin: Option[String]={
    store.get(uinKey).map(_.toStringUtf8)
  }
  protected def startLocalStore(): Unit ={
    store = new LocalStore(storePath)
    store.start()
  }
  protected def stopLocalStore(): Unit ={
    if(store != null)
      store.close()
  }
}
