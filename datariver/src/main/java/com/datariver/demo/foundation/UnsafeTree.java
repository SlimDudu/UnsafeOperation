package com.datariver.demo.foundation;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static com.datariver.demo.utils.UnsafeUtils.*;


@Service
public class UnsafeTree {


    private static AtomicLong sum = new AtomicLong();

    private static long root;

    public long put(long address) {
        if (root != UnsafeConst.EMPTY_ADDRESS) {
            long pre = root;
            long key = US.getLong(address + UnsafeConst.KEY_OFFSET);
            for (long cur = pre, curKey; cur != UnsafeConst.EMPTY_ADDRESS; ) {
                curKey = US.getLong(cur + UnsafeConst.KEY_OFFSET);
                if (key < curKey) {
                    pre = cur;
                    cur = US.getLong(cur + UnsafeConst.LEFT_OFFSET);
                } else if (key > curKey) {
                    pre = cur;
                    cur = US.getLong(cur + UnsafeConst.RIGHT_OFFSET);
                } else {
                    // 说明节点存在于二叉树
                    // TODO 合并操作
                    return cur;
                }
            }
            long preKey = US.getLong(pre + UnsafeConst.KEY_OFFSET);
            if (key < preKey) {
                US.putLong(pre + UnsafeConst.LEFT_OFFSET, address);
            } else {
                US.putLong(pre + UnsafeConst.RIGHT_OFFSET, address);
            }
        } else {
            root = address;
        }
        sum.incrementAndGet();
        return address;
    }

    public long get(long address) {
        if (root != UnsafeConst.EMPTY_ADDRESS) {
            long pre = root;
            long key = US.getLong(address + UnsafeConst.KEY_OFFSET);
            for (long cur = pre, curKey; cur != UnsafeConst.EMPTY_ADDRESS; ) {
                curKey = US.getLong(cur + UnsafeConst.KEY_OFFSET);
                if (key < curKey) {
                    cur = US.getLong(cur + UnsafeConst.LEFT_OFFSET);
                } else if (key > curKey) {
                    cur = US.getLong(cur + UnsafeConst.RIGHT_OFFSET);
                } else {
                    return cur;
                }
            }
        }
        return UnsafeConst.NODE_NOT_FOUND;
    }

    public boolean remove(long address) {
        if (root != UnsafeConst.EMPTY_ADDRESS) {
            long offset =  UnsafeConst.RIGHT_OFFSET;
            long key = US.getLong(address + UnsafeConst.KEY_OFFSET);
            for (long cur = root, curKey, parent = cur; cur != UnsafeConst.EMPTY_ADDRESS; ) {
                curKey = US.getLong(cur + UnsafeConst.KEY_OFFSET);
                if (key < curKey) {
                    parent = cur;
                    cur = US.getLong(cur + UnsafeConst.LEFT_OFFSET);
                    offset = UnsafeConst.LEFT_OFFSET;
                } else if (key > curKey) {
                    parent = cur;
                    cur = US.getLong(cur + UnsafeConst.RIGHT_OFFSET);
                    offset = UnsafeConst.RIGHT_OFFSET;
                } else {
                    return removeNode(cur, parent, offset);
                }
            }

        }
        return false;
    }

    private boolean removeNode(long cur, long parent, long offset) {
        long left = US.getLong(cur + UnsafeConst.LEFT_OFFSET);
        long right = US.getLong(cur + UnsafeConst.RIGHT_OFFSET);
        // 左右节点都存在
        if (left != UnsafeConst.EMPTY_ADDRESS && right != UnsafeConst.EMPTY_ADDRESS) {
            // 声明该节点的后继节点
            long successor;
            // 找到该节点后继节点的父节点
            long successorParent = getSuccessorParent(right);
            if (successorParent == UnsafeConst.EMPTY_ADDRESS) {
                // 后继为 cur 的右节点
                successorParent = cur;
                successor = US.getLong(successorParent + UnsafeConst.RIGHT_OFFSET);
            } else {
                successor = US.getLong(successorParent + UnsafeConst.LEFT_OFFSET);
            }
            if (successor != right) {
                long successorRight = US.getLong(successor + UnsafeConst.RIGHT_OFFSET);
                US.putLong(successorParent + UnsafeConst.LEFT_OFFSET, successorRight);
                US.putLong(successor + UnsafeConst.RIGHT_OFFSET, right);
            }
            // 将后继节点的左子树设置为 cur 的左子树
            US.putLong(successor + UnsafeConst.LEFT_OFFSET, left);
            // 将后继节点与 cur 的前驱建立连接
            return adjustNode(cur, parent, offset, successor);
        }
        // 删除的节点为叶节点
        if (left == UnsafeConst.EMPTY_ADDRESS && right == UnsafeConst.EMPTY_ADDRESS) {
            return adjustNode(cur, parent, offset, UnsafeConst.EMPTY_ADDRESS);
        }
        // 存在左节点，不存在右节点
        if (left != UnsafeConst.EMPTY_ADDRESS && right == UnsafeConst.EMPTY_ADDRESS) {
            return adjustNode(cur, parent, offset, left);
        }
        // 存在右节点，不存在左节点
        if (left == UnsafeConst.EMPTY_ADDRESS && right != UnsafeConst.EMPTY_ADDRESS) {
            return adjustNode(cur, parent, offset, right);
        }
        return false;
    }

    private boolean adjustNode(long cur, long parent, long offset, long node) {
        if (cur == root) {
            root = node;
        } else {
            US.putLong(parent + offset, node);
        }
        return true;
    }

    private long getSuccessorParent(long node) {
        long parent = UnsafeConst.EMPTY_ADDRESS;
        for (long current = US.getLong(node + UnsafeConst.LEFT_OFFSET);
             current != UnsafeConst.EMPTY_ADDRESS;
             current = US.getLong(node + UnsafeConst.LEFT_OFFSET)) {
            parent = node;
            node = current;
        }
        return parent;
    }
}
