package test.webNIO;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

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
public class TestNonBlockingNIO {
    
    // 使用非阻塞式NIO 完成极简版聊天室
    
    // 非阻塞式客户端
    @Test
    public void testNonBlockingClient() {
        
        try (
//                FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898))) {
            
            socketChannel.configureBlocking(false);
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            
//            while(inChannel.read(byteBuffer) != -1) {
//                byteBuffer.flip();
//                socketChannel.write(byteBuffer);
//                byteBuffer.clear();
//            }
            Scanner scanner = new Scanner(System.in);
            
            while(scanner.hasNext()) {
                String str = scanner.next();
                byteBuffer.put((LocalDateTime.now().toString() + "\n" + str).getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                
               
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 非阻塞式服务端
    @Test
    public void testNonBlockingServer() {
        
        try (
//                FileChannel outChannel = FileChannel.open(Paths.get("0.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            //1. 切换至非阻塞式
            serverSocketChannel.configureBlocking(false);
            
            //2. 绑定端口
            serverSocketChannel.bind(new InetSocketAddress(9898));
            
            //3. 获取选择器
            Selector selector = Selector.open();
            
            //4. 将通道注册到选择器中，并且指定监听接收事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            // 5. 轮询选择器上的所有“就绪”事件
            // 如果选择器上的就绪事件大于0个，即已经有事件准备就绪了
            while(selector.select() > 0) {
                //6. 获取当前选择器上的所有准备就绪事件
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                //7. 遍历所有的准备就绪事件
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    //8. 如果就绪事件是接收就绪那么就获取客户端连接
                    if(selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        //9. 将客户端连接切换到非阻塞式模式
                        socketChannel.configureBlocking(false);
                        
                        // 10. 将客户端通道注册到选择器上
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        
                    }else if (selectionKey.isReadable()) {
                        //11. 获取选择器上读就绪状态的通道
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        
                        //12. 读取数据
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
//                        while(channel.read(buffer) != -1) {
//                            buffer.flip();
////                          System.out.println(new String(buffer.array(), 0, buffer.limit()));
//                            outChannel.write(buffer);
//                            buffer.clear();
//                        }
                        int len = 0;
                        while((len = channel.read(buffer)) > 0) {
                            buffer.flip();
                            System.out.println(new String(buffer.array(), 0, len));
                            buffer.clear();
                        }
                       
                        
                    }
                    // 13. 取消选择键 selectionKey
                    iterator.remove();
                    
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
}
