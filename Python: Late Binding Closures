# [Late Binding Closures](http://docs.python-guide.org/en/latest/writing/gotchas/)

Another common source of confusion is the way Python binds
its variables in closures (or in the surrounding global scope).

## Problem Code
```{python}
def create_multipliers():
    return [lambda x : i * x for i in range(5)]

if __name__ == '__main__':
    for multiplier in create_multipliers():
        print multiplier(2),
```

*output*
```
8 8 8 8 8
```

## Explanation
>Python’s closures are late binding.
This means that the values of variables used in closures are looked up at the time the inner function is called.

Here, whenever any of the returned functions are called,
the value of i is looked up in the surrounding scope at call time.
The code above is the same as below:
```{python}
def create_multipliers():
    multipliers = []

    for i in range(5):
        def multiplier(x):
            return i * x
        multipliers.append(multiplier)

    return multipliers
```
By then, the loop has completed and i is left with its final value of 4.

## Solution
Create another argument which is a default argument with value set by i.

```{python}
def create_multipliers():
    return [lambda x, i=i : i * x for i in range(5)]
```
The default argument's value will be set when the function defined.

Alternatively, you can use the functools.partial function:
```{python}
from functools import partial
from operator import mul

def create_multipliers():
    return [partial(mul, i) for i in range(5)]
```

## Further Thinking
1. Python is a *Lexial Scoping* language with *Late Binding*.
So, it's good at closures implementation, which is very useful in lots of situations.
2. Creating functions in loop or other situations like this,
can cause hiccups unfortunately.
