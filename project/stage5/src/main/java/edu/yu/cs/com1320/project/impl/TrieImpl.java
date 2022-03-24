package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
//import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.stream.Collectors;


public class TrieImpl<Value> implements Trie<Value> {

    public TrieImpl() {
    }

    private static final int alphabetSize = 256;
    TrieNode root;


    public static class TrieNode<Value> //Trie  node  constructor
    {
        protected Value val;
        protected List<Value> list = new ArrayList<Value>();
        protected TrieNode[] child = new TrieNode[alphabetSize];


     }

    @Override
    public void put(String key, Object val) {
        if (val == null) {
            //todo -  deleteAll the value from this key
            deleteAll(key);
//            if (this.root == null) {
//             root = new  TrieNode();
//            }
        } else {
            this.root = put(this.root, key.toLowerCase(), (Value) val, 0);
        }
    }


    private TrieNode put(TrieNode x, String key, Value val, int d) {

        if (x == null) {
            x = new TrieNode();
        }

        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (d == key.length()) {
            x.list.add(val);
            return x;
        }
        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.child[c] = this.put(x.child[c], key, val, d + 1);
        return x;
    }

    private TrieNode get(TrieNode x, String key, int d) {
        if (x == null){
            return null;
        }
        if (d == key.length()) {
            return x;
        }
        char c = key.charAt(d);
        return get(x.child[c], key, d + 1);
    }

    @Override
    public List getAllSorted(String key, Comparator<Value> comparator) {
        List nodeList = new ArrayList();
        Set<Value> set = new HashSet<>();

        if (comparator == null){
            return  null;
        }

           TrieNode node = get(root, key, 0);
        if (node == null){
            return nodeList;
        }
        set.addAll(node.list);
        nodeList.addAll(set);

          Collections.sort( nodeList, comparator );

        return nodeList;
    }



    @Override
    public List getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {

       List result = new ArrayList<>();
       List nodeList= new ArrayList();
       Set<Value> duplicates = new HashSet<>();


        for(String str :keysWithPrefix(prefix.toLowerCase())) {
            // all the keys with prefix
            List thisList= get(root, str, 0).list;
            if (nodeList == null){
                return nodeList;
            }
            nodeList.addAll(thisList);

            for(Object uri : nodeList) { // all the Docs in every node
               duplicates.add((Value) uri);
            }
        }
        result.addAll(duplicates);
        Collections.sort( result,comparator );
        return result;
    }

    public Iterable<String> keysWithPrefix(String prefix) {
        //use queue so matches closer to trie root get displayed first
        Queue<String> results = new ArrayDeque<String>();
        //find node which represents the prefix
        TrieNode x = this.get(this.root, prefix, 0);
        //collect keys under it
        if(x!= null) {
            this.collect(x, new StringBuilder(prefix), results);
        }
        return results;
    }


    private void collect(TrieNode x,StringBuilder prefix,Queue<String> results) {
        //if this node has a value, add it’s key to the queue
        if (x.list.size() != 0) {
        //add a string made up of the chars from
        //root to this node to the result set
            results.add(prefix.toString());
        }
        //visit each non-null child/link
        for (char c = 0; c < alphabetSize; c++) {
            if(x.child[c]!=null){
        //add child's char to the string
                prefix.append(c);
                this.collect(x.child[c], prefix, results);
        //remove the child's char to prepare for next iteration
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }
    }






    @Override
    public Set deleteAllWithPrefix(String prefix) {
        Set<Object> result = new HashSet<Object>();

        for(String str : keysWithPrefix(prefix.toLowerCase())) { // all the keys with prefix
           for(Object doc :get(root, str, 0).list) { // all the Docs in every node
               result.add(doc);
           }
       deleteAll(str); // deletes all the nodes in str key
       }
        return result;
    }

    @Override
    public Set deleteAll(String key) { // delete all the list in that node


        TrieNode node = get(root, key.toLowerCase(), 0);
        Set<String> hSet = new HashSet<String>();// new Set
        hSet.addAll(node.list); //Values from List -> Set
        node.list.clear(); // delete List
        return hSet; // return set
    }

    @Override
    public Object delete(String key, Object val) {
        // en ese key, tengo q borrar SOLO ese value !

        this.root = delete(this.root, key.toLowerCase(), 0, (Value)val );
        return val;// el value que se borro
        // si no contiene el Val return null


    }

    private TrieNode delete(TrieNode x, String key, int d, Value val){
        if (x == null){
            return null;
        }

        if (d == key.length()) {

            x.list.remove(val); // poner el list en null / poner el VALUE aqui

        } else {
            char c = key.charAt(d);
            x.child[c] = delete(x.child[c], key, d + 1, val);
        }

        // remove subtrie rooted at x if it is completely empty
        if (x.val != null){
            return x;
        }

        for (int c = 0; c < alphabetSize; c++){
            if (x.child[c] != null) {
                return x;
            }
        }
        return null;
    }
}
