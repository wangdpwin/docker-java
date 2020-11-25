package cn.wangdpwin.docker;

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: wangdongpeng
 * @Date: 2020-10-15 15:54
 * @Description
 * @Version 1.0
 */
public class ClientService {

    private static DockerClient dockerClient;

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    public static void main(String[] args) {
        simpleConnect();
//        certConnect();
        info();
        close();
    }

    private static void info() {
        logger.info("get base info");
        Info info = dockerClient.infoCmd().exec();
        System.out.println(JSON.toJSONString(info,true));
    }

    /**
     * 简单连接
     */
    private static void simpleConnect() {
        if(dockerClient==null) {
            dockerClient = DockerClientBuilder.getInstance("tcp://114.141.150.36:2375").build();
        }else {
            logger.error("docker client is allread init");
        }
    }

    private static void close() {
        try {
            dockerClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用证书连接docker服务
     * 参考 https://www.hangge.com/blog/cache/detail_2548.html#
     * 证书文件
     * ca-key.pem ca.pem     cert.pem   key.pem
     */
    public static void certConnect() {
        //进行安全认证
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerTlsVerify(true)
                .withDockerCertPath("/Users/wangdongpeng/usr/certs/docker/")
                .withDockerHost("tcp://192.168.121.12:2375")
                .withDockerConfig("/Users/wangdongpeng/usr/certs/docker/")
                .build();
        // 连接docker服务器
        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

}
