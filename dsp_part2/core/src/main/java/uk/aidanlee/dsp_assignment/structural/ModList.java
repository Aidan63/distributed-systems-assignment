package uk.aidanlee.dsp_assignment.structural;

import java.util.Iterator;

/**
 * Linked list implementation which allows modifying the list while iterating over it.
 * @param <T> Type this list will hold.
 */
public class ModList<T> implements Iterable<T> {

    /**
     * The head node.
     */
    private ListNode h;

    /**
     * The tail node.
     */
    private ListNode q;

    /**
     * Length of the list.
     */
    private int length;

    /**
     * Creates a new empty list.
     */
    public ModList() {
        length = 0;
    }

    /**
     * Adds an element at the end of this list. Increases length by 1.
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
     * Removes the first occurrence of _item in the list.
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

    /**
     * Returns an iterator for the mod list.
     * @return iterator instance.
     */
    @Override
    public Iterator<T> iterator() {
        return new ModListIterator(h);
    }

    /**
     * Iterator instance which implements the iterator interface.
     */
    private class ModListIterator implements Iterator<T> {

        /**
         * Head node.
         */
        private ListNode head;

        /**
         * Creates a new iterator for a mod list.
         * @param _head The list to iterate over.
         */
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

    /**
     * Mode list node item.
     * Stores the data and a reference to the next node.
     */
    private class ListNode {
        /**
         * Node data.
         */
        T item;

        /**
         * Next node item.
         */
        ListNode next;

        /**
         * Creates a new list node.
         * @param _item Node data.
         * @param _next Next node.
         */
        ListNode(T _item, ListNode _next) {
            item = _item;
            next = _next;
        }
    }
}
