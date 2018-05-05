package test.webNIO;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

/**
 * 一：使用NIO 完成网络通信的三个核心
 * 1. 通道Channel：负责连接
 *      java.nio.channels.channel 接口
 *          |--SelectableChannel
 *              |--SocketChannel
 *              |--ServerSocketChannel
 *              |--DatagramChannel
 *              
 *              |--Pipe.SinkChannel
 *              |--Pipe.SourceChannel
 *  
 * 2. 缓冲区Buffer：负责数据的存取
 * 3. 选择器Selector：是SelectableChannel 的多路复用器。用于监控SelectableChannel 的IO 状况 
 */
public class TestWebNIO {

    /*
     * 服务端等待客户端的连接，当客户端连接上服务端后，不仅需要向服务端发送数据
     * 还需要从服务端接收返回的数据，即客户端程序一直没有结束，导致服务端不知道客户端的数据是否发送完毕
     * 此时服务端无法处理自己的业务逻辑（即从客户端接受数据处理以后再将数据返送给客户端）
     * 这就形成了阻塞状态（如果要解决这种问题，要么通知服务端数据已经发送完毕（twr语法有点问题），要么切换非阻塞式模式）
     * 
     * 而在测试1 中客户端发送完数据后程序就结束了，服务端知道数据已经发送完毕，所以可以顺利执行自己的业务
     */
    // 传统阻塞型网络通信客户端的弊端
    @Test
    public void testClient2() {
        try (FileChannel inChannel = FileChannel.open(Paths.get("2.jpg"), StandardOpenOption.READ);
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9988))) {
            
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            // 将通道的数据写入到缓冲区中
            while(inChannel.read(buffer) != -1) {
                buffer.flip();
                // 将缓冲区中的数据写入到通道中
                socketChannel.write(buffer);
                buffer.clear();
            }
            // 告诉服务器数据已经发送完毕
            socketChannel.shutdownOutput();
            
            // 将服务端传来的数据读取到缓冲区中
            int len = 0;
            while((len = (socketChannel.read(buffer))) != -1) {
                // 切换缓冲区的状态
                buffer.flip();
             
                // 两种写法都可以
//                System.out.println(new String(buffer.array(), 0, buffer.limit()));
                System.out.println(new String(buffer.array(), 0, len));
                // 清空缓冲区的缓存，将position位置重新指向0
                buffer.clear();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
    
    // 传统阻塞型网络通信的服务端弊端
    @Test
    public void testServer2() {
        
        try ( FileChannel outChannel = FileChannel.open(Paths.get("5.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
                ) {
            
            serverSocketChannel.bind(new InetSocketAddress(9988));
            SocketChannel socketChannel = serverSocketChannel.accept();
            
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(socketChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
            
            // 发送反馈给客户端
            buffer.put("服务端接受成功！".getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    
    // 传统阻塞型网络通信客户端
    @Test
    public void testClient() {
        // 从本地读取数据的通道和用于网络通信的通道
        try (FileChannel fileChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9988))) {
            
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(fileChannel.read(buffer) != -1) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
    // 传统阻塞型网络通信服务端
    @Test
    public void testServer() {
        // 服务端的网路通道，本地通道，以及和客户端建立连接的通道（客户端的网络通道）
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)
                ) {
            serverSocketChannel.bind(new InetSocketAddress(9988));
            SocketChannel socketChannel = serverSocketChannel.accept();
            
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(socketChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
    }
    
    
    
    
    
}
