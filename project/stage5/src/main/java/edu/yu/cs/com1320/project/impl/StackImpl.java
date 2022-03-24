package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;


public class StackImpl<T> implements Stack<T> {

   public StackImpl(){

   }

//@
    @SuppressWarnings("unchecked")
    T[] data = (T[]) new Object[5];

      int top = 0;


   T[] doubleSize(T[]data){
       @SuppressWarnings("unchecked")


       int size = data.length;
       Object[] doubledd = new Object[size*2];

       for( int i = 0; i < size; i++){

           doubledd[i]=data[i];
       }
       return (T[]) doubledd;

   }


    @Override
    public void push(T element) {
            int length = data.length;
        if(top == length)
        {
          data = doubleSize(data);
           int nLen = data.length;
           int x = nLen +1;

        }

         data[top] = element;
         top ++;

    }

    @Override
    public T pop() {

            if(top == -1) {
                return null;
            }
            int t = top-1;
            T topItem = data[t];
            data[t] = null;
            top--;
            return topItem;

        }

    @Override
    public T peek() {
       if (top == 0){
           throw new IllegalStateException();
       }
       int t = top-1;
        return data[t];

    }

    @Override
    public int size() {

        return top;
    }
}