package com.datariver.demo.foundation;

import com.datariver.demo.utils.UnsafeUtils.UnsafeConst;

import java.util.concurrent.atomic.AtomicLong;

import static com.datariver.demo.utils.UnsafeUtils.US;

public class UnsafeQueue {

    private static AtomicLong size = new AtomicLong();

    private static long head;

    private static long tail;

    private static final long META_CAPS = UnsafeConst.ADDRESS_CAPS + UnsafeConst.ADDRESS_CAPS;

    public void put(long node) {
        long address = US.allocateMemory(META_CAPS);
        US.putLong(address, node);
        if (head == UnsafeConst.EMPTY_ADDRESS) {
            head = address;
            tail = head;
        } else {
            US.putLong(tail + UnsafeConst.ADDRESS_CAPS, address);
            tail = address;
        }
        size.incrementAndGet();
    }

    public long poll() {
        if (head == UnsafeConst.EMPTY_ADDRESS) {
            return UnsafeConst.EMPTY_ADDRESS;
        }
        long result = US.getLong(head);
        long temp = head;
        head = US.getLong(head + UnsafeConst.ADDRESS_CAPS);
        US.freeMemory(temp);
        size.decrementAndGet();
        return result;
    }

}
