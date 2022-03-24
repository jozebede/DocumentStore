package edu.yu.cs.com1320.project;

public interface HashTable<Key,Value> {


        /**
         * @param k the key whose value should be returned
         * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
         */
        Value get(Key k);

        /**
         * @param k the key at which to store the value
         * @param v the value to store
         * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
         */
        Value put(Key k, Value v);

}
