package test.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.Set;
import java.util.SortedMap;

import org.junit.Test;

/**
 *  一：通道（channel）：用于源节点和目标节点之间的连接，在 java NIO 中负责缓冲区数据的传输，
 *      channel 本身不存储数据，因此需要配合缓冲区来进行数据的传输
 *    
 *  二：通道的主要实现类：
 *      java.nio.channels.Channel 接口
 *          |-- FileChannel
 *          |-- SocketChannel
 *          |-- ServerSocketChannel
 *          |-- DatagramChannel
 *         
 *  三：获取通道
 *      1. java 针对支持通道的类提供了getChannel（）方法----》不推荐使用
 *          本地IO:
 *              FileInputStream/FileOutputStream
 *              RandomAccessFile
 *          网络IO:
 *              Socket
 *              ServerSocket
 *              DatagramSocket
 *      2. 在jdk1.7 中的NIO2 针对各个通道提供了静态方法open（）
 *      3. 在jdk1.7 中的NIO2 的Files 工具类的newByteChannel（）
 *      
 *  四：通道之间的数据传输
 *  transferForm()
 *  transferTo()
 *  
 *  五：分散（Scatter）与聚集（Gather）
 *      分散读取（Scatter Reads）：将通道中的数据分散到多个缓冲区（依次）
 *      聚集写入（Gather Writes）：将多个缓冲区的数据聚集到通道中
 *      
 *  六：NIO 中使用字符集（如果编码器和解码器不一致，则乱码）
 *  编码：字符串--》字节数组
 *  解码：字节数组--》字符串
 */

public class TestChannel {
    
    
    @Test
    public void testChannel6() throws CharacterCodingException {
        Charset charset = Charset.forName("GBK");
        // 编码器
        CharsetEncoder newEncoder = charset.newEncoder();
        // 解码器
        CharsetDecoder newDecoder = charset.newDecoder();
        
        String string = "我爱你!";
        CharBuffer charBuffer = CharBuffer.allocate(1024);
        charBuffer.put(string);
        charBuffer.flip();
        
        // 编码成字节（改用java8 的流式操作，这里有点麻烦，因为需要将每个字节预先包装为包装类）
        ByteBuffer byteBuffer = newEncoder.encode(charBuffer);
        List<Byte> list = new ArrayList<>();
        for (int i = 0; i <byteBuffer.limit() ; i++) {
//            System.out.println(byteBuffer.get());
            list.add(byteBuffer.get());
        }
        Stream<Byte> stream = list.stream();
        stream.forEach(System.out::println);
        
        // 解码成字符串
        byteBuffer.flip();
        CharBuffer decode = newDecoder.decode(byteBuffer);
        System.out.println(decode.toString());
        
        /*IntBuffer intBuffer = IntBuffer.allocate(122);
        intBuffer.put(12);
        System.out.println(intBuffer.get(0));*/
        
        
    }
    
    @Test
    public void TestChannel5() {

        // 获取到所有可用的字符集编码格式
        SortedMap<String, Charset> map = Charset.availableCharsets();
        Set<Entry<String, Charset>> entrySet = map.entrySet();
        
        /*for (Entry<String, Charset> entry : entrySet) {
            System.out.println(entry.getKey()+"---"+entry.getValue());
        }*/
        
        // 使用java8的流式处理和lanmbda 表达式更加方便
        entrySet.forEach(System.out::println);
    }
    
    // 分散读取和聚集写入
    @Test
    public void TestChannel4() {
        try ( RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");
                FileChannel inChannel = randomAccessFile.getChannel();
                RandomAccessFile randomAccessFile2 = new RandomAccessFile("2.txt", "rw");
                FileChannel outChannel = randomAccessFile2.getChannel()) {
            
            ByteBuffer buffer = ByteBuffer.allocate(100);
            ByteBuffer buffer2 = ByteBuffer.allocate(1024);
            ByteBuffer[] dsts = {buffer, buffer2};
            // 分散读取
            inChannel.read(dsts);
            
            for (ByteBuffer byteBuffer : dsts) {
                // 切换模式，从缓冲区读取数据《重点》
                byteBuffer.flip();
            }
            
            // 如果乱码需要设置文件格式为utf-8
            System.out.println(new String(dsts[0].array(), 0, dsts[0].limit()));
            System.out.println("---------------------------");
            System.out.println(new String(dsts[1].array(), 0, dsts[1].limit()));
            
            // 聚集写入
            outChannel.write(dsts);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        
    }
    
    
    // 测试通道之间直接数据传输，还是使用了直接缓冲区，但是更加简便
    @Test
    public void TestChannel3() {
        try (FileChannel inChannel = FileChannel.open(Paths.get("C:/File/movies/djjh.mp4"), StandardOpenOption.READ);
                FileChannel outChannel = FileChannel.open(Paths.get("C:/File/movies/1.mp4"), StandardOpenOption.WRITE,
                        StandardOpenOption.READ, StandardOpenOption.CREATE)) {
//            inChannel.transferTo(0, inChannel.size(), outChannel);
            outChannel.transferFrom(inChannel, 0, inChannel.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    // 使用直接缓冲区完成文件的复制（内存映射文件）：速度很快，但是如果垃圾回收机制不及时回收，会导致数据已经复制完毕，但是程序依然引用物理内存（但是无法管理物理内存，这是操作系统负责的）
    @Test
    public void TestChannel2() {
        Instant now = Instant.now();
        
        try (FileChannel inChannel = FileChannel.open(Paths.get("C:/File/movies/djjh.mp4"), StandardOpenOption.READ);
                FileChannel outChannel = FileChannel.open(Paths.get("C:/File/movies/1.mp4"), StandardOpenOption.WRITE,
                        StandardOpenOption.READ, StandardOpenOption.CREATE)) {
            // 内存映射文件
            MappedByteBuffer inMap = inChannel.map(MapMode.READ_ONLY, 0, inChannel.size());
            MappedByteBuffer outMap = outChannel.map(MapMode.READ_WRITE, 0, inChannel.size());

            // 直接对缓冲区进行数据的读写操作
            byte[] buffer = new byte[inMap.limit()];
            // 将数据从inMap 中取出来到字节数组中
            // 将字节数组中的数据放进到outMap 中
            inMap.get(buffer);
            outMap.put(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Instant now2 = Instant.now();
        // java8 使用新的时间API : 1154
        System.out.println("直接缓冲区消耗的时间是：" + Duration.between(now, now2).toMillis());    

    }
    
    
    // 通过通道完成文件的复制(非直接缓冲区)
    @Test
    public void testChannel1() {
         LocalTime now = LocalTime.now();
        // 这里使用 twr(try with resource) 语法更加简介美观
        try (FileInputStream fis = new FileInputStream("C:\\File\\movies\\djjh.mp4");
                FileOutputStream fos = new FileOutputStream("C:\\File\\movies\\1.mp4");
                FileChannel inChannel = fis.getChannel();
                FileChannel outChannel = fos.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                // 一定要记得刷新缓冲区
                buffer.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalTime now2 = LocalTime.now();
        // java8 使用新的时间API : 5773
        System.out.println("非直接缓冲区消耗时间：" + Duration.between(now, now2).toMillis());
    }

}
