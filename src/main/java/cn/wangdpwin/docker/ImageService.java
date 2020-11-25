package cn.wangdpwin.docker;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @Author: wangdongpeng
 * @Date: 2020-10-16 14:46
 * @Description
 * @Version 1.0
 * @link https://www.hangge.com/blog/cache/detail_2547.html
 */
public class ImageService {
    private DockerClient dockerClient;

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

    public static void main(String[] args) {
        ImageService imageService = new ImageService();
        DockerClient dockerClient = imageService.connectDocker();
        imageService.queryImages(dockerClient);
        imageService.exsist(dockerClient);
        imageService.searchImage(dockerClient);
        imageService.pullImage("hello-world:latest");
//        imageService.close();
        try {
            Thread.sleep(60 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步拉取镜像
     * @param name
     */
    public void pullImage(String name) {
        dockerClient.pullImageCmd("busybox:latest").exec(new PullImageResultCallback()).awaitSuccess();
//        System.out.println("下载完毕!");

        //异步
        dockerClient.pullImageCmd(name).exec(new ResultCallback<PullResponseItem>() {
            @Override
            public void onStart(Closeable closeable) {
                System.out.println("开始下载!");
            }
            @Override
            public void onNext(PullResponseItem object) {
                // 实时显示出下载信息
                System.out.println(object.getStatus());
            }
            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
            @Override
            public void onComplete() {
                System.out.println("下载完毕!");
            }
            @Override
            public void close() throws IOException {
                System.out.println("关闭!");
            }
        });
    }

    public void removeImage(String name) {
        dockerClient.removeImageCmd(name).exec();
        System.out.println("删除完毕");
    }

    public List<SearchItem> searchImage(DockerClient dockerClient) {
        List<SearchItem> dockerSearch = dockerClient.searchImagesCmd("redis").exec();
        for(SearchItem item : dockerSearch) {
            System.out.println(item.getName());
        }
        return dockerSearch;
    }

    public void exsist(DockerClient dockerClient) {
        List<Image> images = dockerClient.listImagesCmd().withImageNameFilter("redis").exec();
        if (images.isEmpty()) {
            System.out.println("不存在 redis 镜像。");
        } else {
            System.out.println("存在 redis 镜像。");
        }
    }

    public List<Image> queryImages(DockerClient dockerClient) {
        List<Image> images = dockerClient.listImagesCmd().exec();
        for(Image image : images) {
            System.out.println(image.getRepoTags()[0]);
        }
        return images;
    }


    /**
     * 容器命名：将运行的容器命名为 hangge_http_server
     *     端口绑定：Apache 端口是 80，将其映射到主机的 8080 端口
     *     Data Volume 实现数据持久化：/usr/local/apache2/htdocs 是 Apache Server 存放静态文件的地方，我们将服务器上的 /home/user/hangge/htdocs 目录 mount 到容器中将其取代。
     */
    public void createContainer(DockerClient dockerClient) {
        //创建容器
        CreateContainerResponse container1 = dockerClient.createContainerCmd("httpd:latest")
                .withName("hangge_http_server") //给容器命名
                .withPortBindings(PortBinding.parse("8080:80")) //Apache端口是80，映射到主机的8080端口
                .withBinds(Bind.parse("/home/user/hangge/htdocs:/usr/local/apache2/htdocs")) //目录挂载
                .exec();

        //运行容器
        dockerClient.startContainerCmd(container1.getId()).exec();
    }

    /**
     * 进入容器执行命令
     * @param dockerClient
     * @param containerId
     * @param cmd ls pwd echo .....
     */
    public void exec(DockerClient dockerClient, String containerId, String cmd) {
        // 创建命令
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd("bash", "-c", cmd)
                .exec();
        // 执行命令
        dockerClient.execStartCmd(execCreateCmdResponse.getId()).exec(
                new ExecStartResultCallback(System.out, System.err));
    }

}
