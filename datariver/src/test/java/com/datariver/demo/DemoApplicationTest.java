package com.datariver.demo;

import com.datariver.demo.foundation.UnsafeQueue;
import com.datariver.demo.foundation.UnsafeTree;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static com.datariver.demo.utils.UnsafeUtils.US;

@SpringBootTest
class DemoApplicationTest {

    private static final long[] nodes = new long[]{5, 1, 10, 2, 8};

    @Test
    void testUnsafeTree() throws InterruptedException {
        UnsafeTree tree = new UnsafeTree();
        List<Long> list = new ArrayList<>();
        for (long node : nodes) {
            long address = US.allocateMemory(24);
            US.setMemory(address, 24, (byte) 0);
            US.putLong(address, node);
            long result = tree.put(address);
            list.add(address);
            System.out.println(US.getLong(result));
        }
        for (long address : list) {
            if (tree.remove(address)) {
                US.freeMemory(address);
            }
        }
    }

    @Test
    void testUnsafeQueue() {
        UnsafeQueue queue = new UnsafeQueue();
        for (long node : nodes) {
            long address = US.allocateMemory(8);
            US.setMemory(address, 8, (byte) 0);
            US.putLong(address, node);
            queue.put(address);
        }
        for (int i = 0; i < 5; i++) {
            long result = queue.poll();
            System.out.println(US.getLong(result));
        }
        System.out.println(queue.poll());
    }

}
