The differences between `Vector` and `ArrayList` are some like this:

1. `Vector` is synchronized while `ArrayList` is not synchronized. So, Vector is thread safe. 
2. `Vector` is slow as it is thread safe . In comparison `ArrayList` is fast as it is non-synchronized.
3. A `Vector` grows as doubling size of its array in default. While when you insert an element into the `ArrayList`, it increases its Array size by 50% .

```
      * ArrayList:  

            /**
             * Increases the capacity to ensure that it can hold at least the
             * number of elements specified by the minimum capacity argument.
             *
             * @param minCapacity the desired minimum capacity
             */
            private void grow(int minCapacity) {
               // overflow-conscious code
               int oldCapacity = elementData.length;
               int newCapacity = oldCapacity + (oldCapacity >> 1); // 50%
               if (newCapacity - minCapacity < 0)
                  newCapacity = minCapacity;
               if (newCapacity - MAX_ARRAY_SIZE > 0)
                  newCapacity = hugeCapacity(minCapacity);
               // minCapacity is usually close to size, so this is a win:
                  elementData = Arrays.copyOf(elementData, newCapacity);
            }


      * Vector:

            private void grow(int minCapacity) {
              // overflow-conscious code
              int oldCapacity = elementData.length;
              int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                             capacityIncrement : oldCapacity); // default 100%
              if (newCapacity - minCapacity < 0)
                  newCapacity = minCapacity;
              if (newCapacity - MAX_ARRAY_SIZE > 0)
                  newCapacity = hugeCapacity(minCapacity);
              elementData = Arrays.copyOf(elementData, newCapacity);
            }
        
```

4. ArrayList does not define the increment size . Vector defines the increment size .

```
            /**
             * The amount by which the capacity of the vector is automatically
             * incremented when its size becomes greater than its capacity.  If
             * the capacity increment is less than or equal to zero, the capacity
             * of the vector is doubled each time it needs to grow.
             *
             * @serial
             */
             protected int capacityIncrement;
 ```

Based above:

1. `ArrayList` capable can not resize just like `Vector`.
2. `ArrayList` is not thread safe. It can not replace `Vector` by `ArrayList` in multiple threads directly. 
3. It can replace `Vector` by `ArrayList` in single thread mostly. Because The declaration of `Vector` and `ArrayList`:

```
        public class Vector<E>
            extends AbstractList<E>
            implements List<E>, RandomAccess, Cloneable, java.io.Serializable  


        public class ArrayList<E> 
            extends AbstractList<E>
            implements List<E>, RandomAccess, Cloneable, java.io.Serializable   
```
