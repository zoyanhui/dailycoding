# Problem
The sample code below, what is the output do you think?

```{python}
def test_default(x, mylist = []):
    mylist.append(x)
    print str(mylist)

if __name__ == '__main__':
    test_default(x = 1)
    test_default(x = 2)
```

*output*
```
[1]
[1, 2]
```
What's the problem?

The para named mylist has been changed at the second call.

Why?

# [Python Common Gotchas](http://docs.python-guide.org/en/latest/writing/gotchas/)
>Python’s default arguments are evaluated once when the function is defined,
not each time the function is called (like it is in say, Ruby).
This means that if you use a mutable default argument and mutate it,
you will and have mutated that object for all future calls to the function as well.

# Solution
Create a new object each time the function is called,
by using a default arg to signal that no argument was provided (None is often a good choice).

```{python}
def test_default(x, mylist = None):
    if mylist == None:
        mylist = []
    mylist.append(x)
    print str(mylist)

if __name__ == '__main__':
    test_default(x = 1)
    test_default(x = 2)
```

# Further Thinking
1. Variable of python is a pointer to a memory.
The operation with the variable has same steps:
    - Get the value at the memory
    - Calculate with the value
    - Store the output value to a new memory
Then, set the new value to the prior variable,
will cause the variable(pointer) to point to the new memory.

2. Function in python is not only piece of code, it's a object.
And the default arguments may be internal members of the object.
But the detail implementation may be only got from the editor of Python.
