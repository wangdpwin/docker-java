# src 
简单连接
https://www.hangge.com/blog/cache/detail_2547.html  
证书连接
https://www.hangge.com/blog/cache/detail_2548.html#

##使用证书进行安全连接
  1，创建证书
  （1）首先选择一个存放密钥文件的地方（我这里选择 /home/user/certs），执行如下命令创建文件夹，并进入该文件夹：
  mkdir /home/user/certs
  cd /home/user/certs
  
  （2）执行如下命令生成 CA 私钥，过程中会要求输入密码（这里我输入 123456）：
  openssl genrsa -aes256 -out ca-key.pem 4096
  
  （3）接着执行如下命令生成 CA 公钥，也就是证书。一开始会要求输入前面设置的密码：
  openssl req -new -x509 -days 365 -key ca-key.pem -sha256 -out ca.pem
  注意：过程中 Common Name 填写服务器的 ip 地址。其它几项就随便填了。
  原文:SpringBoot - 使用docker-java远程管理docker教程2（使用证书进行安全连接）
  
  （4）接着执行如下命令生成服务器私钥：
  openssl genrsa -out server-key.pem 4096
  
  （5）接着执行如下命令用私钥生成服务器公钥请求文件，也就是证书：
  注意：CN 后面的 IP 地址即为前面我们设置的服务器 IP。
  openssl req -subj "/CN=192.168.60.133" -sha256 -new -key server-key.pem -out server.csr
  
  （6）接着执行如下配置哪些主机可以访问 docker：
  这里我们可以填写 IP 地址或者 DNS name。 0.0.0.0 代表所有主机都可以通过密钥文件的方式访问
  echo subjectAltName = DNS:192.168.60.133,IP:192.168.60.133,IP:0.0.0.0,IP:127.0.0.1 >> extfile.cnf
  
  （7）接着执行如下命令：
  echo extendedKeyUsage = serverAuth >> extfile.cnf
  
  （8）接着执行如下命令将上述多个生成信息，写入文件（期间同样会要求输入密码）。
  openssl x509 -req -days 365 -sha256 -in server.csr -CA ca.pem -CAkey ca-key.pem  -CAcreateserial -out server-cert.pem -extfile extfile.cnf
  
  （9）接着执行如下命令生成客户端私钥：
  openssl genrsa -out key.pem 4096
  
  （10）接着执行如下命令生成客户端证书请求文件：
  openssl req -subj '/CN=client' -new -key key.pem -out client.csr
  
  （11）接着执行如下命令：
  echo extendedKeyUsage = clientAuth >> extfile.cnf
  
  （12）接着执行如下命令用 CA 为客户端签署证书文件（期间同样会要求输入密码）：
  openssl x509 -req -days 365 -sha256 -in client.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -out cert.pem -extfile extfile.cnf
  
  （13）执行如下命令删除临时文件 ：
  rm -v client.csr server.csr
  
  （14）执行如下命令配置证书权限：
  chmod -v 0400 ca-key.pem key.pem server-key.pem
  chmod -v 0444 ca.pem server-cert.pem cert.pem
  
  （15）查看 /home/user/certs 下的文件，一共应该有 8 个文件：
  原文:SpringBoot - 使用docker-java远程管理docker教程2（使用证书进行安全连接）
  
  2，服务端设置
  （1）首先执行如下命令编辑服务器上的 docker.service 文件。
  vi /lib/systemd/system/docker.service
  
  （2）找到 Execstart=/usr/bin/dockerd，并在后加上如下内容，然后保存退出。
  -D --tlsverify=true --tlscert=/home/user/certs/server-cert.pem --tlskey=/home/user/certs/server-key.pem --tlscacert=/home/user/certs/ca.pem -H tcp://0.0.0.0:2375 -H unix://var/run/docker.sock
  原文:SpringBoot - 使用docker-java远程管理docker教程2（使用证书进行安全连接）
  
  （3）接着运行以下命令，重启 docker 服务。
  systemctl daemon-reload
  service docker restart//重启启动docker
  
  （4）执行如下命令可以查看相关内容，看看设置是否生效：
  systemctl status docker
  原文:SpringBoot - 使用docker-java远程管理docker教程2（使用证书进行安全连接）
  
  3，客户端设置
  （1）首先将 ca-key.pem、ca.pem、cert.pem、key.pem 这 4 个密钥文件下载到本机的磁盘上，这里我将其放在 /Users/hangge/dockerCerts 目录下。
  原文:SpringBoot - 使用docker-java远程管理docker教程2（使用证书进行安全连接）
  
  （2）然后 Java 这边，使用证书进行安全连接即可。下面代码在连接 docker 服务器后，获取相关信息并打印出来
  
## 指定容器ip
```
1、首先创建子网（主机192.168.1.3中执行）
docker network create --subnet=172.18.0.0/16 mynetwork
查看创建子网
docker network ls
2、分配ip（主机192.168.1.3中执行）
docker run -itd --name test --net mynetwork --ip 172.18.0.3 centos:6.7 bash
可以exec进入容器查看ip确实分配成功
进入容器：
docker exec -it test /bin/bash
```
