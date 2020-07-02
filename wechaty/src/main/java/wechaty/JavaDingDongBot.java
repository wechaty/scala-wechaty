package wechaty;

/**
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2020-06-04
 */
public class JavaDingDongBot {
  public static void main(String[] args) throws InterruptedException {
    WechatyOptions option = new WechatyOptions();
    Wechaty bot = Wechaty.instance(option);
    bot
      .onScan(payload -> System.out.println(String.format("Scan QR Code to login: %s\nhttps://wechaty.github.io/qrcode/%s\n", payload.status().toString(), payload.qrcode())))
      .onLogin(payload -> System.out.println(String.format("User %s logined", payload.id())))
      .onMessage(message -> {
//					if (message.payload().type() != Message.MessageType.MessageTypeText() || !message.payload().text().equals("#ding")){
        if (!message.payload().text().equals("#ding")) {
          System.out.println("Message discarded because it does not match #ding");
        } else {
          System.out.println("send message to " + message.payload().fromId());
          message.say("dong");
          System.out.println("dong");
        }
      });


    bot.start();

    Thread.currentThread().join();
  }
}
