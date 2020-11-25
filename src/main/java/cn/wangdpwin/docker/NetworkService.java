package cn.wangdpwin.docker;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * @Author: wangdongpeng
 * @Date: 2020-10-16 15:07
 * @Description
 * @Version 1.0
 */
public class NetworkService {
    private DockerClient dockerClient;

    public DockerClient connectDocker(){
        if(dockerClient==null) {
            dockerClient = DockerClientBuilder.getInstance("tcp://192.168.121.12:2375").build();
            Info info = dockerClient.infoCmd().exec();
            String infoStr = JSONObject.toJSONString(info);
            System.out.println("docker的环境信息如下：=================");
            System.out.println(info);
        }else {
            System.out.println("docker client is allready init");
        }
        return dockerClient;
    }

    /**
     * 自定义网络
     * 通过 bridge 驱动创建一个名为 java-docker-mssql 的自定义网络：
     */
    public void CreateNetwork() {
        dockerClient = connectDocker();
        CreateNetworkResponse networkResponse = dockerClient.createNetworkCmd()
                .withName("java-docker-mssql")
                .withDriver("bridge").exec();
    }


    /**
     * 在运行容器的时候使用 java-docker-mssql 这个自定义网络：
     */
    public void CreateContainerWithNetwork() {
        dockerClient = connectDocker();
        //创建容器
        CreateContainerResponse container1 = dockerClient.createContainerCmd("busybox")
                .withNetworkMode("java-docker-mssql") //设置网络
                .exec();
        //运行容器
        dockerClient.startContainerCmd(container1.getId()).exec();
    }

}
