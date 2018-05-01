package uk.aidanlee.dsp.common.structural;

import java.util.Iterator;

/**
 * Map implementation which allows adding and removing while iterating over it.
 * @param <K>
 * @param <V>
 */
public class ModMap<K, V> implements Iterable<K> {
    /**
     * Underlying list structure.
     */
    private ModList<Entry> items;

    /**
     * Creates a new mod map.
     */
    public ModMap() {
        items = new ModList<>();
    }

    /**
     * Adds a key and value to the map.
     * @param _key   The key use.
     * @param _value The value to place.
     */
    public void set(K _key, V _value) {
        items.add(new Entry(_key, _value));
    }

    /**
     * Gets the value in the map at a specific key.
     * @param _key The key value.
     * @return Value object or null.
     */
    public V get(K _key) {
        for (Entry e : items) {
            if (e.key.equals(_key)) return e.value;
        }

        return null;
    }

    /**
     * Removes the value in the map at a specific key.
     * @param _key The key value.
     */
    public void remove(K _key) {
        for (Entry e : items) {
            if (e.key.equals(_key)) {
                items.remove(e);
                return;
            }
        }
    }

    /**
     * Checks if a key exists within the map.
     * @param _key Key to check.
     * @return Boolean.
     */
    public boolean exists(K _key) {
        for (Entry e : items) {
            if (e.key.equals(_key)) return true;
        }

        return false;
    }

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
