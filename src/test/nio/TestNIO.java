package test.nio;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 *一：缓冲区(Buffer):在 javaNIO 中负责数据的存储，缓冲区其实底层就是数组，用于存储不同数据类型的数据
 *
 *根据数据类型的不同，提供不同类型的缓冲区（除 boolean 外）
 *  ByteBuffer
 *  CharBuffer
 *  ShortBuffer
 *  IntBuffer
 *  LongBuffer
 *  FloatBuffer
 *  DoubleBuffer
 * 
 * 上述缓冲区的管理方式几乎一致，可以通过 allocate() 方法获取缓冲区（最常用的就是ByteBuffer）
 * 
 * 二：缓冲区存取数据的两个核心方法
 * put():存入数据到缓冲区中
 * get():获取缓冲区中的数据
 * 
 * 三：缓冲区中的四个核心属性
 * 1. capacity：表示缓冲区的最大存储数据的容量，一旦声明，不可修改
 * 2. limit：表示缓冲区可以操作数据的最大大小，及limit后的数据就不可以操作了
 * 3. position：表示缓冲区正在操作数据的位置、初始值为0
 * 4. mark：标记，用于记录当前position 的位置，可以通过 reset() 恢复到mark 记录的位置
 * 
 * 0 <= mark <= position <= limit <= capacity
 * 
 * 四：直接缓冲区和非直接缓冲区
 * 1. 非直接缓冲区：通过 allocate（）方法分配缓冲区，将缓冲区建立在 JVM 的内存中
 * 2. 直接缓冲区：通过allocateDirect（）方法分配直接缓冲区，即缓冲区建立在物理内存中，可以提高效率
 */
public class TestNIO {

    @Test
    public void testNIO3() {
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        System.out.println(buffer.isDirect());
    }
    
    @Test
    public void TestNIO2() {
        String string = "abcd";
        
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(string.getBytes());
        
        buffer.flip();
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst, 0, 2);
        System.out.println(new String(dst, 0, 2));
        System.out.println(buffer.position());
        
        // 使用mark（）方法标记当前position 的位置
        buffer.mark();
        buffer.get(dst, 2, 2);
        System.out.println(new String(dst, 2, 2));
        System.out.println(buffer.position());
        
        // reset（）将position 位置重置到mark 记录的位置
        buffer.reset();
        System.out.println(buffer.position());
        
        // 如果缓冲区还有剩余的数据（根据position 的位置判断）
        if(buffer.hasRemaining()) {
            // 打印缓冲区中可以操作数据的数量
            System.out.println("可以操作的数据还有：" + buffer.remaining());
        }
        
    }
    
    @Test
    public void testNIO1() {
        String str = "abcde";
        
        // 1. 分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        System.out.println("--------allocate------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        
        // 2. 利用 put（） 存入字节流数据
        buffer.put(str.getBytes());
        System.out.println("--------put------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        
        // 3. 利用 flip 切换成读取数据的模式
        buffer.flip();
        System.out.println("--------flip------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        
        // 4. 利用get（）读取缓冲区中的数据
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst);
        System.out.println("--------get------");
        System.out.println(new String(dst, 0, dst.length));
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        
        // 5. 利用rewind（）方法可重新存储数据
        buffer.rewind();
        System.out.println("--------rewind------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        
        // 6. 清空缓冲区,但是缓冲区中的数据依然存在，处于“被遗忘状态”
        buffer.clear();
        System.out.println("--------clear------");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
        System.out.println((char)buffer.get());
    
  }
    
    
}
