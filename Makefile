# Makefile for Scala Wechaty
#
# 	GitHb: https://github.com/wechaty/scala-wechaty
# 	Author: Huan LI <zixia@zixia.net> https://github.com/huan
#

.PHONY: all
all : clean test

.PHONY: clean
clean:
	mvn clean

.PHONY: lint
lint:
	echo lint

.PHONY: install
install:
	mvn install

.PHONY: test
test:
	mvn test

.PHONY: bot
bot:
	mvn \
		-DWECHATY_PUPPET_HOSTIE_TOKEN=$$WECHATY_PUPPET_HOSTIE_TOKEN \
		-Dmaven.test.skip=true \
		-P run verify
.PHONY: padbot
padbot:
	mvn \
		-DWECHATY_PUPPET_PADPLUS_TOKEN=$$WECHATY_PUPPET_PADPLUS_TOKEN \
		-Dmaven.test.skip=true \
		-P run-pad verify

.PHONY: version
version:
	@newVersion=$$(awk -F. '{print $$1"."$$2"."$$3+1}' < VERSION) \
	&& echo $${newVersion} > VERSION \
	&& git add VERSION \
    && mvn versions:set -DnewVersion=$${newVersion} \
    && mvn versions:commit \
    && sed -i '' 's/<tag>HEAD<\/tag>/<tag>$${newVersion}<\/tag>/' pom.xml \
	&& git commit -a -m "Release $${newVersion}" > /dev/null \
	&& git tag "$${newVersion}" \
	&& echo "Bumped version to $${newVersion}" \
	&& newVersion=$$(awk -F. '{print $$1"."$$2"."$$3+1}' < VERSION) \
    && mvn versions:set -DnewVersion=$${newVersion}-SNAPSHOT \
    && mvn versions:commit \
	&& git commit -a -m "prepare next development $${newVersion}-SNAPSHOT" > /dev/null \
	&& echo "next version to $${newVersion}-SNAPSHOT"
