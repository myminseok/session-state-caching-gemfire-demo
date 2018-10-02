# Pivotal Cloud Cache를 활용한 tomcat session state caching 예제

* https://docs.pivotal.io/p-cloud-cache/1-4/using-pcc.html
* https://github.com/Pivotal-Field-Engineering/pivotal-gemfire-spring-session

---
## PCC의 서비스url이 HTTPS인 경우 gfsh client의 환경의 truststore에 인증서가 저장되어 있어야 한다. 다음은 그 절차를 설명한다.

 PCF PAS cert를 다운로드

opsmanager>pas tile> settings> networking>  Certificate and Private Key for HAProxy and Router

#### 기존 trust store 백업 (mac)
~~~
sudo cp $(/usr/libexec/java_home)/jre/lib/security/cacerts $(/usr/libexec/java_home)/jre/lib/security/cacerts.orig
~~~

#### truststore 생성(mac)
~~~
sudo keytool -import -alias pcfdemo-ssl -file ./pcfdemo.net.cert -keystore $(/usr/libexec/java_home)/jre/lib/security/cacerts
password 는 임의로 입력
~~~


---
## PCC 서비스 인스턴스 생성하기

~~~
cf create-service p-cloudcache dev-plan pcc-small
~~~

---
## PCC에 session정보를 저장할 region(=table같은 것)을 생성하기

#### gfsh다운로드
gemfire version에 맞는 gfsh를 사용해야함. v9.3.0

현재 PCC gemfire version: https://docs.pivotal.io/p-cloud-cache/1-4/index.html#snapshot
network.pivotal.io> gemfire에서 pivotal-gemfire-9.3.0.zip

####  pivotal-gemfire-9.3.0.zip의 압축해제
cd pivotal-gemfire-9.3.0/bin

#### gfsh실행전에 java trust store 환경변수 선언(mac)
~~~
export JAVA_ARGS="-Djavax.net.ssl.trustStore=$(/usr/libexec/java_home)/jre/lib/security/cacerts”
~~~

#### PCC접속 및 region(=table같은 것)생성
~~~
gfsh>connect --use-http --url=https://cloudcache-8d53ce5b-cb87-480c-8ad4-08ec1751ca22.system.pcfdemo.net/gemfire/v1 --user=cluster_operator_xxxxx --password=<password>
Successfully connected to: GemFire Manager HTTP service @ org.apache.geode.management.internal.web.http.support.HttpRequester@4b760141

Cluster-0 gfsh> create region --name=test --type=PARTITION --entry-idle-time-expiration=180 --entry-idle-time-expiration-action=INVALIDATE --enable-statistics=true

~~~

---
## sample app push

~~~
cf push
~~~


