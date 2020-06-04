# Makefile for Scala Wechaty
#
# 	GitHb: https://github.com/wechaty/scala-wechaty
# 	Author: Huan LI <zixia@zixia.net> https://github.com/huan
#

.PHONY: all
all : clean lint

.PHONY: clean
clean:
	echo clean

.PHONY: lint
lint:
	echo lint

.PHONY: install
install:
	echo install

.PHONY: test
test:
	echo test

.PHONY: bot
bot:
	mvn \
		-DWECHATY_PUPPET_HOSTIE_TOKEN=$$WECHATY_PUPPET_HOSTIE_TOKEN \
		-Dexec.mainClass=wechaty.DingDongBot verify

.PHONY: version
version:
	@newVersion=$$(awk -F. '{print $$1"."$$2"."$$3+1}' < VERSION) \
		&& echo $${newVersion} > VERSION \
		&& git add VERSION \
		&& git commit -m "$${newVersion}" > /dev/null \
		&& git tag "v$${newVersion}" \
		&& echo "Bumped version to $${newVersion}"
