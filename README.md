全新的MQTT 推送服务套件,考虑网页接入的可能性，添加了websocket 网关和HTTP网关。
pushserver 只管转发报文，pushadmin接管上下线状态和消息发送失败的情况。

![输入图片说明](https://images.gitee.com/uploads/images/2018/1006/222044_2e3758b5_75292.jpeg "幻灯片1.JPG")



打包并且复制依赖包到lib
mvn clean dependency:copy-dependencies package -Dmaven.test.skip=true -DincludeScope=runtime    -DoutputDirectory=lib

启动网关
setsid java -jar mqtt-getway-0.0.1-SNAPSHOT.jar  > getway.log

启动推送服务
setsid java -jar mqtt-server-0.0.1-SNAPSHOT.jar  > push.log

