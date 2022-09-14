package org.itstack.demo.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author:
 * @CreateTime: 2022-09-14  13:53
 * @Description: 服务端
 */
public class NioServer {

    private Integer port;

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private static ByteBuffer buffer = ByteBuffer.allocate(1024);

    public NioServer(Integer port) throws IOException {

        //创建选择器
        selector = Selector.open();
        //创建一个ServerSocketChannel
        serverSocketChannel = ServerSocketChannel.open();
        //绑定端口
        serverSocketChannel.bind(new InetSocketAddress(port));
        //设置为非阻塞状态
        serverSocketChannel.configureBlocking(false);
        //注册事件到选择器中
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        System.out.println("NioServer start ....");
        while (true) {
            int select = selector.select();
            if (select == 0){
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            //3.轮询SelectionKey
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            if (iterator.hasNext()){
                SelectionKey nextKey = iterator.next();
                //如果满足Acceptable条件，则必定是一个ServerSocketChannel
                if (nextKey.isAcceptable()){
                    ServerSocketChannel sscTemp = (ServerSocketChannel) nextKey.channel();
                    //得到一个连接好的SocketChannel，并把它注册到Selector上，兴趣操作为READ
                    SocketChannel socketChannel = sscTemp.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("REGISTER CHANNEL , CHANNEL NUMBER IS:" + selector.keys().size());
                }
                if (nextKey.isReadable()){
                    //读取数据
                    SocketChannel channel = (SocketChannel) nextKey.channel();
                    readFromChannel(channel);
                }
                //4.remove SelectionKey
                iterator.remove();
            }
        }

    }

    private static void readFromChannel(SocketChannel channel) {
        int count ;
        buffer.clear();
        try {
            while ((count = channel.read(buffer)) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                System.out.println("READ FROM CLIENT:" + new String(bytes));
            }
            if (count < 0) {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new NioServer(1234).start();
    }

}
