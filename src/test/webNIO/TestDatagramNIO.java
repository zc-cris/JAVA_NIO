package test.webNIO;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

import org.junit.Test;

public class TestDatagramNIO {
    
    // UDP 的发送端
    @Test
    public void testSend() {
        try (DatagramChannel datagramChannel = DatagramChannel.open()) {
            
            datagramChannel.configureBlocking(false);
            
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNext()) {
                buffer.put((LocalDateTime.now().toString()+"\n"+scanner.next()).getBytes());
                buffer.flip();
                datagramChannel.send(buffer, new InetSocketAddress("127.0.0.1", 9988));
                buffer.clear();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // UDP 的接收端
    @Test
    public void testReceive() {
        try (DatagramChannel datagramChannel = DatagramChannel.open()) {
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(9988));
            
            // 开启选择器并注册监听事件
            Selector selector = Selector.open();
            datagramChannel.register(selector, SelectionKey.OP_READ);
            
            // 轮训监听事件，如果满足注册的监听事件，就做出处理
            while(selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    if(next.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        
                        datagramChannel.receive(buffer);
                        buffer.flip();
                        System.out.println(new String(buffer.array(), 0, buffer.limit()));
                        buffer.clear();
                    }
                }
                // 一定要移除掉
                iterator.remove();
            }
         
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    

}
