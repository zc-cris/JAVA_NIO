package test.webNIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;

import org.junit.Test;

public class TestPipe {

    @Test
    public void testPipe1() throws IOException {
        Pipe pipe = Pipe.open();
        
        try (SinkChannel sinkChannel = pipe.sink();
                SourceChannel sourceChannel = pipe.source()) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
           // 将缓冲区数据写入专门的管道
            buffer.put("保持饥饿，大智若愚".getBytes());
            buffer.flip();
            sinkChannel.write(buffer);
            System.out.println(buffer.limit());
            System.out.println(buffer.position());
            
            buffer.flip();
//            System.out.println(buffer.limit()+"----"+buffer.position());
//            System.out.println(new String(buffer.array(), 0, buffer.limit()));
            // 将专门的管道里的数据读取进缓冲区中
            sourceChannel.read(buffer);
            System.out.println(new String(buffer.array(), 0 , buffer.limit()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
    }
    
    
}
