package uk.aidanlee.dsp_assignment.structural;

import org.jetbrains.annotations.NotNull;
import java.util.Iterator;

public class ModMap<K, V> implements Iterable<K> {
    /**
     * Underlying list structure.
     */
    private ModList<Entry> items;

    public ModMap() {
        items = new ModList<>();
    }

    public void set(K _key, V _value) {
        items.add(new Entry(_key, _value));
    }

    public V get(K _key) {
        for (Entry e : items) {
            if (e.key.equals(_key)) return e.value;
        }

        return null;
    }

    public void remove(K _key) {
        for (Entry e : items) {
            if (e.key.equals(_key)) {
                items.remove(e);
                return;
            }
        }
    }

    public boolean exists(K _key) {
        for (Entry e : items) {
            if (e.key.equals(_key)) return true;
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<K> iterator() {
        return new ModMapIterator(items);
    }

    private class Entry {
        private K key;
        private V value;

        Entry(K _key, V _value) {
            key   = _key;
            value = _value;
        }
    }

    private class ModMapIterator implements Iterator<K> {
        private Iterator<Entry> it;

        ModMapIterator(ModList _list) {
            it = _list.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public K next() {
            Entry e = it.next();
            return e.key;
        }
    }
}
