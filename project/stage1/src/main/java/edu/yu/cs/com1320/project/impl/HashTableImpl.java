package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.net.URI;


public class HashTableImpl<Key,Value> implements HashTable<Key,Value> {

    Generic[] kArray = new Generic[5];


    public static class Generic<Key, Value> {


        public Key key;
        public Value value;
        public Generic next;


        Generic(Key key, Value value) {
            this.key = key;
            this.value = value;
        }

         Value getValue() {
            return this.value;
        }

        void setValue(Value newVal) {
            this.value = newVal;

        }
    }

    @Override
    public Value get(Key key) {

        int index = Math.abs(key.hashCode()) % 5;

        Generic current = kArray[index];



        if (kArray[index] == null) { // check of there is at least one Generic in list
            return null;
        }

        while (current != null) {
            if (current.key == key) {
                return (Value) current.getValue(); // return the VALUE of key
            }
            current = current.next;// goes through every element
        }
        return null;
    }


    @Override
    public Value put(Key key, Value value) {
        if (value == null){
            //this.delete(key);
        }

        int index = Math.abs(key.hashCode())% 5; // position in array

        if (kArray[index] != null) {
            Generic current = kArray[index]; //current for the first element in array

            while (current.next != null) {  // goes to the last element in the list
                current = current.next;
                if (current.key == key) { // check for existance of the same key allready in this list
                Value name = (Value)current.getValue();
                    current.setValue(value);

                    return (Value)name; // returns the last value with tat key
                }
            }
            current.next = new Generic(key, value); // adds the new Generic at the end of the list

        } else { // is the first element in the list
            kArray[index] = new Generic(key, value);
        }


        return null; // if the key was not already present.


    }
}


