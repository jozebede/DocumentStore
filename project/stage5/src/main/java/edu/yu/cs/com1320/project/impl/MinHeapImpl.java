package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;

import java.util.*;

import static java.util.Arrays.copyOfRange;

public class MinHeapImpl<E extends Comparable> extends MinHeap {



    public MinHeapImpl() {
    }


    @SuppressWarnings("unchecked")
    protected E[] elements = (E[]) new Comparable[5]; // documents
    protected int count = 0;
    protected Map<E, Integer> elementsToArrayIndex = new HashMap<E, Integer>(); //used to store the index in the elements array




        @Override
        public void reHeapify(Comparable element) {



            int index = elementsToArrayIndex.get(element);
            int right = index * 2 + 2;
            int left = index * 2 + 1;
            int father = index/2;
            //maybe sean null. q pasaria?

            // todo - ver como es el caso de que baje el minHeap ... remove min

            if (!isLeaf(index)) {

                if (isGreater(index, right) || isGreater(index, left)) {

                    if (isGreater(right, left)) {
                        swap(index,left);
                        reHeapify(elements[left]);
                    }
                       swap(index,right);
                       reHeapify(elements[right]);
                }
            }
            if(elements[father] != null) {
                if (isGreater(father, index)) {
                    swap(father, index);
                    // cabie esto. prestar atencion
                    reHeapify(elements[father]);
                }
            }
        }


        @Override
        protected int getArrayIndex(Comparable element) {

            int index = elementsToArrayIndex.get(element);

            return index;
        }


        @Override
        protected void doubleArraySize() {

            E[] copy = Arrays.copyOf(elements, elements.length * 2);
            elements = copy;
        }


        protected boolean isEmpty() {
            return this.count == 0;
        }

        private boolean isLeaf(int index) {
          int size = elementsToArrayIndex.size();
            if (index >= (size / 2) && index <= size) {
                return true;
            }
            return false;
        }


        protected boolean isGreater(int i, int j) {
            if (elements[i] == null) {
                return false;
            }
            return this.elements[i].compareTo(this.elements[j]) > 0;
        }


         protected void swap(int i, int j) {

            elementsToArrayIndex.remove(elements[i]);
            elementsToArrayIndex.remove(elements[j]);
            E temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
            elementsToArrayIndex.put(elements[i], i);
            elementsToArrayIndex.put(elements[j], j);
        }


        protected void upHeap(int k) {

            while (k > 1 && this.isGreater(k / 2, k)) {
                this.swap(k, k / 2);
                k = k / 2;

            }
        }

        protected void downHeap(int k) {
            while (2 * k <= this.count) {
                //identify which of the 2 children are smaller
                int j = 2 * k;
                if (j < this.count && this.isGreater(j, j + 1)) {
                    j++;
                }
                //if the current value is < the smaller child, we're done
                if (!this.isGreater(k, j)) {
                    break;
                }
                //if not, swap and continue testing
                this.swap(k, j);
                k = j;
            }
        }


        public void insert(Comparable x) {
            // double size of array if necessary
            if (elements == null || this.count >= this.elements.length - 1) {
                this.doubleArraySize();
            }
            //add x to the bottom of the heap
            this.elements[++this.count] = (E) x;
            elementsToArrayIndex.put((E) x, count); // agrega elementos al map.
            //percolate it up to maintain heap order property
            this.upHeap(this.count);
        }


        public E removeMin() {

            if (isEmpty()) {
                throw new NoSuchElementException("Heap is empty");
            }
            int minIndex = 1;
            E min = this.elements[minIndex];
            //swap root with last, decrement count
            this.swap(minIndex, this.count--);
            //move new root down as needed
            this.downHeap(minIndex);

            this.elements[this.count + 1] = null; //null it to prepare for GC
            elementsToArrayIndex.remove(min); // removes from MAP.

            return min;

        }



    }


// MinHeap has to store something that both has the URI and will be compared to other
// instances based on the last used time in the document stored in the BTree.





