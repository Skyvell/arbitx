package arbitx.contracts;

import score.Context;
import score.DictDB;
import com.iconloop.score.util.EnumerableSet;

public class EnumerableMap<K, V> {
    private final EnumerableSet<K> keys;
    private final DictDB<K, V> values;

    public EnumerableMap(String id, Class<K> keyClass, Class<V> valueClass) {
        this.keys = new EnumerableSet<K>(id + "_keys", keyClass);
        this.values = Context.newDictDB(id, valueClass);
    }

    public int size() {
        return keys.length();
    }

    public boolean contains(K key) {
        return keys.contains(key);
    }

    public K getKey(int index) {
        return keys.at(index);
    }

    public V get(K key) {
        return values.get(key);
    }

    public V getOrThrow(K key, String msg) {
        var entry = this.get(key);
        if (entry != null) {
            return entry;
        }
        Context.revert(msg);
        return null; // should not reach here, but made compiler happy
    }

    public V getOrDefault(K key, V value) {
        var entry = this.get(key);
        return entry != null ? entry : value;
    }

    public void set(K key, V value) {
        values.set(key, value);
        keys.add(key);
    }

    public void remove(K key) {
        values.set(key, null);
        keys.remove(key);
    }
}