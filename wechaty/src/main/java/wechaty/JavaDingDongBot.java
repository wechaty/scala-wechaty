package wechaty;

import wechaty.puppet.schemas.Message;

import static jdk.nashorn.internal.objects.Global.println;

/**
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2020-06-04
 */
public class JavaDingDongBot {
	public static void main(String[] args) throws InterruptedException {
		WechatyOptions option = new WechatyOptions();
		Wechaty bot = Wechaty.instance(option);
		bot
				.onScan(payload -> {
					System.out.println(payload);
					System.out.println(String.format("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n",payload.status().toString(), payload.qrcode()));
				})
				.onLogin(payload -> {
					System.out.println(String.format("User %s logined",payload.id()));
				})
				.onMessage(message -> {
					System.out.println(message.payload().type());
//					if (message.payload().type() != Message.MessageType.MessageTypeText() || !message.payload().text().equals("#ding")){
					if (!message.payload().text().equals("#ding")){
						System.out.println("Message discarded because it does not match #ding");
					}else{
						System.out.println("send message to "+ message.payload().fromId());
						message.say("dong");
						System.out.println("dong");
					}
				});


		bot.start();

		Thread.currentThread().join();
	}
}
