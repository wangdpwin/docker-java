package cn.wangdpwin.docker;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import java.io.IOException;

/**
 * @Author: wangdongpeng
 * @Date: 2020-10-15 16:40
 * @Description
 * @Version 1.0
 */
public class ContainerService {

    private DockerClient dockerClient = null;

    public CreateContainerResponse createContainers(DockerClient client,String containerName,String imageName){
        //映射端口8080—>80
        ExposedPort tcp80 = ExposedPort.tcp(6666);
        Ports portBindings = new Ports();
        portBindings.bind(tcp80, Ports.Binding.bindPort(8080));

        CreateContainerResponse container = client.createContainerCmd(imageName)
                .withIpv4Address("192.168.121.11")
                .withName(containerName)
                .withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings))
                .withExposedPorts(tcp80).exec();
        return container;
    }


    /**
     * 启动容器
     * @param client
     * @param containerId
     */
    public void startContainer(DockerClient client, String containerId){
        client.startContainerCmd(containerId).exec();
    }

    /**
     * 停止容器
     * @param client
     * @param containerId
     */
    public void stopContainer(DockerClient client,String containerId){
        client.stopContainerCmd(containerId).exec();
    }

    /**
     * 删除容器
     * @param client
     * @param containerId
     */
    public void removeContainer(DockerClient client,String containerId){
        client.removeContainerCmd(containerId).exec();
    }
    public static void main(String[] args){
        ContainerService containerService =new ContainerService();
        //连接docker服务器
        DockerClient client = containerService.connectDocker();
//        dockerClientService.stopContainer(client,"a901bbcb5597");
//        dockerClientService.removeContainer(client,"a901bbcb5597");
//        dockerClientService.startContainer(client,"a901bbcb5597");
        //创建容器并启动
        CreateContainerResponse response= containerService.createContainers(client,"myjava8","mskj/java8-base:latest");
        containerService.startContainer(client,response.getId());
        containerService.close();
    }

    public DockerClient connectDocker(){
        if(dockerClient==null) {
            dockerClient = DockerClientBuilder.getInstance("tcp://114.141.150.36:2375").build();
            Info info = dockerClient.infoCmd().exec();
            String infoStr = JSONObject.toJSONString(info);
            System.out.println("docker的环境信息如下：=================");
            System.out.println(info);
        }else {
            System.out.println("docker client is allready init");
        }
        return dockerClient;
    }

    private void close() {
        try {
            dockerClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
