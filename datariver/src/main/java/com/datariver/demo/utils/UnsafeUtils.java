package com.datariver.demo.utils;

import sun.misc.Unsafe;

public class UnsafeUtils {
    public static Unsafe US = org.springframework.objenesis.instantiator.util.UnsafeUtils.getUnsafe();

    public static class UnsafeConst {

        public static final long EMPTY_ADDRESS = 0;

        public static final long NODE_NOT_FOUND = -1;

        public static final long ADDRESS_CAPS = 8;

        public static final long KEY_OFFSET = 0;

        public static final long LEFT_OFFSET = KEY_OFFSET + ADDRESS_CAPS;

        public static final long RIGHT_OFFSET = LEFT_OFFSET + ADDRESS_CAPS;
    }
}
