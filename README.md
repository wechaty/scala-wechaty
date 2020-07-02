# scala-wechaty

[![Scala CI](https://github.com/wechaty/scala-wechaty/workflows/Scala%20CI/badge.svg)](https://github.com/wechaty/scala-wechaty/actions?query=workflow%3A%22Scala+CI%22)

![Scala Wechaty](https://wechaty.github.io/scala-wechaty/images/scala-wechaty.png)

[![Scala Wechaty Getting Started](https://img.shields.io/badge/Scala%20Wechaty-Getting%20Started-890)](https://github.com/wechaty/scala-wechaty-getting-started)
[![Scala 2.13](https://img.shields.io/badge/scala-2.13+-890.svg)](https://www.scala-lang.org/)
[![Wechaty in Scala](https://img.shields.io/badge/Wechaty-Scala-890)](https://github.com/wechaty/scala-wechaty)

## Connecting Chatbots

[![Powered by Wechaty](https://img.shields.io/badge/Powered%20By-Wechaty-brightgreen.svg)](https://github.com/Wechaty/wechaty)

Wechaty is a Conversatioanl AI RPA SDK for Wechat **Individual** Accounts that help you create a chatbot in 6 lines of Python.

## Voice of the Developers

> "Wechaty is a great solution, I believe there would be much more users recognize it." [link](https://github.com/Wechaty/wechaty/pull/310#issuecomment-285574472)  
> &mdash; <cite>@Gcaufy, Tencent Engineer, Author of [WePY](https://github.com/Tencent/wepy)</cite>
>
> "太好用，好用的想哭"  
> &mdash; <cite>@xinbenlv, Google Engineer, Founder of HaoShiYou.org</cite>
>
> "最好的微信开发库" [link](http://weibo.com/3296245513/Ec4iNp9Ld?type=comment)  
> &mdash; <cite>@Jarvis, Baidu Engineer</cite>
>
> "Wechaty让运营人员更多的时间思考如何进行活动策划、留存用户，商业变现" [link](http://mp.weixin.qq.com/s/dWHAj8XtiKG-1fIS5Og79g)  
> &mdash; <cite>@lijiarui, Founder & CEO of Juzi.BOT.</cite>
>
> "If you know js ... try Wechaty, it's easy to use."  
> &mdash; <cite>@Urinx Uri Lee, Author of [WeixinBot(Python)](https://github.com/Urinx/WeixinBot)</cite>

See more at [Wiki:Voice Of Developer](https://github.com/Wechaty/wechaty/wiki/Voice%20Of%20Developer)

## Join Us

Wechaty is used in many ChatBot projects by thousands of developers. If you want to talk with other developers, just scan the following QR Code in WeChat with secret code _scala wechaty_, join our **Wechaty Python Developers' Home**.

![Wechaty Scala Developers' Home](https://wechaty.github.io/wechaty/images/bot-qr-code.png)

Scan now, because other Wechaty Python developers want to talk with you too! (secret code: _scala wechaty_)

## The World's Shortest Scala ChatBot: 9 lines of Code

```scala
package wechaty

object DingDongBot {
  def main(args: Array[String]): Unit = {
    Wechaty.instance()
      .onScan(payload     => { println("Scan QR Code to login: %s\nhttps://wechaty.github.io/qrcode/%s\n".format(payload.status, payload.qrcode)) })
      .onLogin(payload    => { println("User %s logined\n".format(payload.id)) })
      .onMessage(message  => { println(message) })
      .start()
    Thread.currentThread().join()
  }
}
```

## Scala Wechaty Developing Plan

We already have Wechaty in TypeScript, It will be not too hard to translate the TypeScript(TS) to Python(PY) because [wechaty](https://github.com/wechaty/wechaty) has only 3,000 lines of the TS code, they are well designed and de-coupled by the [wechaty-puppet](https://github.com/wechaty/wechaty-puppet/) abstraction. So after we have translated those 3,000 lines of TypeScript code, we will almost be done.

As we have already a ecosystem of Wechaty in TypeScript, so we will not have to implement everything in Python, especially, in the Feb 2020, we have finished the [@chatie/grpc](https://github.com/chatie/grpc) service abstracting module with the [wechaty-puppet-hostie](https://github.com/wechaty/wechaty-puppet-hostie) implmentation.

The following diagram shows out that we can reuse almost everything in TypeScript, and what we need to do is only the block located at the top right of the diagram: `Wechaty (Scala)`.

```ascii
  +--------------------------+ +--------------------------+
  |                          | |                          |
  |   Wechaty (TypeScript)   | |    Wechaty (Scala)       |
  |                          | |                          |
  +--------------------------+ +--------------------------+

  +-------------------------------------------------------+
  |                 Wechaty Puppet Hostie                 |
  |                                                       |
  |                (wechaty-puppet-hostie)                |
  +-------------------------------------------------------+

+---------------------  @chatie/grpc  ----------------------+

  +-------------------------------------------------------+
  |                Wechaty Puppet Abstract                |
  |                                                       |
  |                   (wechaty-puppet)                    |
  +-------------------------------------------------------+

  +--------------------------+ +--------------------------+
  |      Pad Protocol        | |      Web Protocol        |
  |                          | |                          |
  | wechaty-puppet-padplus   | |(wechaty-puppet-puppeteer)|
  +--------------------------+ +--------------------------+
  +--------------------------+ +--------------------------+
  |    Windows Protocol      | |       Mac Protocol       |
  |                          | |                          |
  | (wechaty-puppet-windows) | | (wechaty-puppet-macpro)  |
  +--------------------------+ +--------------------------+
```

## Example: How to Translate TypeScript to Scala

There's a 100 lines class named `Image` in charge of downloading the WeChat image to different sizes.

It is a great example for demonstrating how do we translate the TypeScript to Scala in Wechaty Way:

### Image Class Source Code

- TypeScript: <https://github.com/wechaty/wechaty/blob/master/src/user/image.ts>
- Scala: <https://github.com/wechaty/scala-wechaty/blob/master/wechaty/src/main/scala/wechaty/user/Image.scala>

If you are interested in the translation and want to look at how it works, it will be a good start from reading and comparing those two `Image` class files in TypeScript and Scala at the same time.

## To-do List

- TS: TypeScript
- SLOC: Source Lines Of Code

### Wechaty Internal Modules

1. [ ] Class Wechaty @wj-mCat
    - TS SLOC(1160): <https://github.com/wechaty/wechaty/blob/master/src/wechaty.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Contact
    - TS SLOC(804): <https://github.com/wechaty/wechaty/blob/master/src/user/contact.ts>
    - [√] Code
    - [√] Unit Tests
    - [ ] Documentation
1. [ ] Class ContactSelf
    - TS SLOC(199): <https://github.com/wechaty/wechaty/blob/master/src/user/contact-self.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Message
    - TS SLOC(1054): <https://github.com/wechaty/wechaty/blob/master/src/user/message.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Room
    - TS SLOC(1194): <https://github.com/wechaty/wechaty/blob/master/src/user/room.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Image @wj-mCat
    - TS SLOC(60): <https://github.com/wechaty/wechaty/blob/master/src/user/image.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [x] Class Accessory @huan
    - TS SLOC(179): <https://github.com/wechaty/wechaty/blob/master/src/accessory.ts>
    - [x] Code
    - [x] Unit Tests
    - [ ] Documentation
1. [ ] Class Config @wj-mCat
    - TS SLOC(187): <https://github.com/wechaty/wechaty/blob/master/src/config.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Favorite
    - TS SLOC(52): <https://github.com/wechaty/wechaty/blob/master/src/user/favorite.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Friendship
    - TS SLOC(417): <https://github.com/wechaty/wechaty/blob/master/src/user/friendship.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class MiniProgram
    - TS SLOC(70): <https://github.com/wechaty/wechaty/blob/master/src/user/mini-program.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class RoomInvitation
    - TS SLOC(317): <https://github.com/wechaty/wechaty/blob/master/src/user/room-invitation.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class Tag
    - TS SLOC(190): <https://github.com/wechaty/wechaty/blob/master/src/user/tag.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class UrlLink
    - TS SLOC(107): <https://github.com/wechaty/wechaty/blob/master/src/user/url-link.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation

### Wechaty External Modules

1. [ ] Class FileBox
    - TS SLOC(638): <https://github.com/huan/file-box/blob/master/src/file-box.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class MemoryCard
    - TS SLOC(376): <https://github.com/huan/memory-card/blob/master/src/memory-card.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class WechatyPuppet
    - TS SLOC(1115): <https://github.com/wechaty/wechaty-puppet/blob/master/src/puppet.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation
1. [ ] Class WechatyPuppetHostie
    - TS SLOC(909): <https://github.com/wechaty/wechaty-puppet-hostie/blob/master/src/grpc/puppet-client.ts>
    - [ ] Code
    - [ ] Unit Tests
    - [ ] Documentation

## Usage

[![Scala Wechaty Getting Started](https://img.shields.io/badge/Scala%20Wechaty-Getting%20Started-red)](https://github.com/wechaty/scala-wechaty-getting-started)

Scala Wechaty is very easy to use!

## Requirements

1. Scala 2.13+

## Development

```shell
git clone git@github.com:wechaty/scala-wechaty.git

export WECHATY_PUPPET_HOSTIE_TOKEN=your_token_here
make bot
```

## History

### master

### v0.4 (Jun 19, 2020)

Scala Wechaty **BETA** Released

Read more from our Multi-language Wechaty Beta Release event from our blog:

- [Multi Language Wechaty Beta Release Announcement!](https://wechaty.github.io/2020/06/19/multi-language-wechaty-beta-release/)

### v0.0.1 (Jun 3, 2020)

Thanks for our community contributor [@jcai](https://github.com/jcai/) Jun Tsai, who is a very experienced Scala developer, start from scratch three days ago (Jun 1) and finished the first version for ding-dong-bot.scala on two days ago, which only last 1 days for all the translation tasks!

#### One day

Only one day! This is the fastest record in the world for translating the TypeScript Wechaty to Multi-language Wechaty, Scala in this case. Cheers!

## Related Projects

- [Wechaty](https://github.com/wechaty/wechaty) - Conversatioanl AI Chatot SDK for Wechaty Individual Accounts (TypeScript)
- [Python Wechaty](https://github.com/wechaty/python-wechaty) - Python WeChaty Conversational AI Chatbot SDK for Wechat Individual Accounts (Python)
- [Go Wechaty](https://github.com/wechaty/go-wechaty) - Go WeChaty Conversational AI Chatbot SDK for Wechat Individual Accounts (Go)
- [Java Wechaty](https://github.com/wechaty/java-wechaty) - Java WeChaty Conversational AI Chatbot SDK for Wechat Individual Accounts (Java)
- [Scala Wechaty](https://github.com/wechaty/scala-wechaty) - Scala WeChaty Conversational AI Chatbot SDK for WechatyIndividual Accounts (Scala)

## Badge

[![Wechaty in Scala](https://img.shields.io/badge/Wechaty-Scala-890)](https://github.com/wechaty/scala-wechaty)

```md
[![Wechaty in Scala](https://img.shields.io/badge/Wechaty-Scala-890)](https://github.com/wechaty/scala-wechaty)
```

## Contributors

[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/0)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/0)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/1)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/1)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/2)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/2)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/3)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/3)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/4)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/4)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/5)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/5)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/6)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/6)
[![contributor](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/images/7)](https://sourcerer.io/fame/huan/wechaty/scala-wechaty/links/7)

## Author

[@jcai](https://github.com/jcai/) Jun Tsai <jcai@ganshane.com>

## Copyright & License

- Code & Docs © 2020 Wechaty Contributors <https://github.com/wechaty>
- Code released under the Apache-2.0 License
- Docs released under Creative Commons
