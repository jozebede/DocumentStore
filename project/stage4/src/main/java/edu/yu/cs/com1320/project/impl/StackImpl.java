package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;


public class StackImpl<T> implements Stack<T> {

//@
    @SuppressWarnings("unchecked")
    T[] data = (T[]) new Object[5];

      int top = 0;


// todo - revisar esto
   T[] doubleSize(T[]data){
       @SuppressWarnings("unchecked")
       T[] doubled = (T[]) new Object[data.length *2];

       for( int i = 0; i < data.length; i++){

           doubled[i]=data[i];
       }
       return doubled;

   }


    @Override
    public void push(T element) {
            int length = data.length  ;
        if(top == length)
        {
           doubleSize(data);
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
            T topItem = data[top];
            data[top] = null;
            top--;
            return topItem;

        }

    @Override
    public T peek() {
        return data[top];

    }

    @Override
    public int size() {

        return data.length;
    }
}