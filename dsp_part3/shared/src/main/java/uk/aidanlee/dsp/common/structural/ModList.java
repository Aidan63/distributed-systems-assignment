package uk.aidanlee.dsp.common.structural;

import java.util.Iterator;

/**
 * Linked list implementation which allows modifying the list while iterating over it.
 * @param <T> Type this list will hold.
 */
public class ModList<T> implements Iterable<T> {
    private ListNode h;
    private ListNode q;
    private int length;

    /**
     * Creates a new empty list.
     */
    public ModList() {
        length = 0;
    }

    /**
     * Adds an element at the end of this list.
     * Increases length by 1.
     * @param _item The item to add.
     */
    public void add(T _item) {
        ListNode x = new ListNode(_item, null);
        if (h == null) {
            h = x;
        }
        else {
            q.next = x;
        }

        q = x;
        length++;
    }

    /**
     * Adds an element at the beginning of this list.
     * @param _item The item to add.
     */
    public void push(T _item) {
        ListNode x = new ListNode(_item, h);
        h = x;

        if (q == null) {
            q = x;
        }

        length++;
    }

    /**
     * Removes the first occurent of _item in the list.
     * @param _item The item to remove.
     */
    public void remove(T _item) {
        ListNode prev = null;
        ListNode l = h;

        while (l != null) {
            if (l.item == _item) {
                if (prev == null) {
                    h = l.next;
                } else {
                    prev.next = l.next;
                }

                if (q == l) {
                    q = prev;
                }

                length--;
                return;
            }

            prev = l;
            l = l.next;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ModListIterator(h);
    }

    private class ModListIterator implements Iterator<T> {
        private ListNode head;

        private ModListIterator(ListNode _head) {
            head = _head;
        }

        @Override
        public boolean hasNext() {
            return head != null;
        }

        @Override
        public T next() {
            T val = head.item;
            head = head.next;

            return val;
        }
    }

    private class ListNode {
        public T item;
        public ListNode next;

        ListNode(T _item, ListNode _next) {
            item = _item;
            next = _next;
        }
    }
}
