package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.MinHeap;

import java.util.*;

import static java.util.Arrays.copyOfRange;

public class MinHeapImpl<E extends Comparable> extends MinHeap {

    public MinHeapImpl() {
    }
    @SuppressWarnings("unchecked")
    protected E[] elements = (E[]) new Comparable[5]; // documents
    protected int count = 0;
    protected Map<E, Integer> elementsToArrayIndex = new HashMap<E,Integer>(); //used to store the index in the elements array



        // como hago con delete? para sacar algo del heap?
        // busco index de E en el map, llevo ese index a ser el min heap y  borro minheap;
        // si solo tengo acceso a remove min, tengo q poder encontrar la forma de llevarlo al min ...




    @Override
    public void reHeapify(Comparable element) {
        // ver si solo con eso basta
        // Document whose time was updated should stay where it is, move up in the heap, or move down in the heap,

      int index = elementsToArrayIndex.get(element);
     //   upHeap(index);
        downHeap(index);
    }


    @Override
    protected int getArrayIndex(Comparable element) {

       int index =  elementsToArrayIndex.get(element);

        return index;
    }



    @Override
   protected void doubleArraySize() {

        E[] copy = Arrays.copyOf(elements,elements.length*2 );
        elements = copy;
    }



    protected  boolean isEmpty() {
        return this.count == 0;
    }

    /**
     * is elements[i] > elements[j]?
     */
    protected  boolean isGreater(int i, int j) {

        if ( elements[i] == null){
            return false;
        }
//        if ( elements[j] == null){
//           return true;
//   }
        return this.elements[i].compareTo(this.elements[j]) > 0;
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */
    protected  void swap(int i, int j) {

        elementsToArrayIndex.remove(elements[i]);
        elementsToArrayIndex.remove(elements[j]);
        E temp = elements[i];
       elements[i] = elements[j];
        elements[j] = temp;
        elementsToArrayIndex.put(elements[i], i );
        elementsToArrayIndex.put(elements[j], j);
    }


    protected  void upHeap(int k) {

        while (k > 1 && this.isGreater(k / 2, k))
        {
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    protected  void downHeap(int k)
    {
        while (2 * k <= this.count)
        {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1))
            {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j))
            {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }


    public void insert(Comparable x) {
        // double size of array if necessary
        if ( elements == null || this.count >= this.elements.length - 1 ){
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = (E) x;
        elementsToArrayIndex.put((E) x,count); // agrega elementos al map.
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }



    public E removeMin() {

        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        int minIndex=1;
        E min = this.elements[minIndex];
        //swap root with last, decrement count
        this.swap(minIndex, this.count--);
        //move new root down as needed
        this.downHeap(minIndex);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        elementsToArrayIndex.remove(count); // removes from MAP.
        return min;
    }
}

