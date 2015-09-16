# Lexical vs. Dynamic Scoping
The scoping rules of a ProgrammingLanguage dictate how FreeVariables - symbol names which are found in the body of a scope (a function, macro, class, whatever) but not defined there - are resolved.

There are several different strategies:

1. Very rare language disallow free references, except pre-defined set of symbols(called keywords or special forms).

2. Two scoping level only, global and local. Like C and many assemblers.

3. Lexical Scoping. If a variable isn't found in a given scope, the enclosing scope is searched; repeating until the outermost scope is reached. Used by CommonLisp, SchemeLanguage, AlgolLanguage, PascalLanguage, and many other modern languages.

4. DynamicScoping (early dialects of Lisp, CommonLisp special variables, exported environment variables in UnixOs): The caller is checked for a binding for the variable; if one is found, it is used. Otherwise, the caller's caller is checked, and so on. If no definition is found, it is either an error or a default value is used, depending on the semantics of the language.

## Lexical Scoping
**Lexical Scoping** (also called static scoping) searches the symbol from defined scope. Name resolution depends on the location in the source code and the lexical context, which is defined by where the named variable or function is defined. *e.g.:*
- R language

```
y <- 10
f <- function(x){
   y <- 2
   y^2 + g(x)
}

g <- function(x){
   x * y
}
print(f(3))
```

- Python

```
y=10
def f(x):
  y = 2
  return y^2 + g(x)

def g(x):
  return x*y

print f(3)
```

The code above have serval implementations, written by **R**, **Python**.

Call *f* function will get this, *f(3) = 34*.
Because, *y* of *g* is searched from defined scope(global environment), *y=10*.
So, when call *g(x)* in *f*, value of *y* in *g* equals to *10*.

Other Lexical Scoping languages. Like **javascript** and **C**. e.g.:
- javascript

```
var y = 10;
function f(x){
   var y = 2;
   return y^2 + g(x);
}
function g(x){
   return x*y;
}
console.log(f(3))
```
Or C code implementation:
- C

```
#include <stdio.h>
int y = 10;

int g(x){
    return x*y;
}

int f(x){
    int y = 2;
    return y^2 + g(x);
}

int main(){
   printf("%d", f(3));
}
```
Above *C* and *javascript* code both will print 34.
- Perl
```
#!/usr/bin/perl
$y = 10;
print(&f(3));
sub f{
    $y = 2;
    return $y^2 + g($_[0]);
}
sub g{
    return $y * $_[0];
}
```

The *Perl* code will print 10 (deferent from *Python* and *R*), because global $y's value is changed in f. But *Perl* is using Lexical Scoping. The difference between *Perl* and *R*, *Python* is that *R* and *Python* use redefining a local symbol to deal with the symbol which is the same name as global(outermost) one, and *Python* provide a keyword *global* to reference to global one. But *Perl* directly use the global one.

Other languages that support lexical scoping: *Scheme*, *Common Lisp (all languages converge to Lisp)*

In **lexical Scoping**, every function keeps a pointer to its defined environment. Any free symbols will be searched from this defined environment, or ancestor environment of defined.

## Dynamic Scoping
But, other languages using **Dynamic Scoping** work different, which search symbol from called area at running time. *e.g.:*
```
y <- 10
f <- function(x){
   y <- 2
   y^2 + g(x)
}

g <- function(x){
   x * y
}
print(f(3)) # print 10, if in DynamicScoping
print(y)    # print 10
```

If above code using **DynamicScoping** will print *10 (2^2 + 2 * 3)*. Call *g* in *f*, symbol y's binding value is the *y* defined in f. But next print(y) will print the global y value, 10.

Dynamic scoping is useful as a substitute for globally scoped variables. A function can say "let current_numeric_base = 16; call other functions;" and the other functions will all print in hexadecimal. Then when they return, and the base-setting function returns, the base will return to whatever it was.
So yes, writing a macro to save, change and restore arbitrary global variables actually implements dynamic scoping. Global variables don't really have lexical scope, however, so it's not "in terms of" lexical scoping. Unfortunately, dynamic scoping (along with global variables) begins to break down in multi-threaded situations.If you just save the variable, assign to it, and restore it, it won't be thread safe, and it will require a special binding construct. Real SpecialVariables work with any binding construct: LET, MULTIPLE-VALUE-BIND, WITH-OPEN-FILE, etc; the special nature of the binding is remembered as a property of the symbol and applied accordingly. And in Lisp implementations that support threads, the bindings are thread-specific! Implementing thread-specific storage is not trivial. If you assign to *standard-output*, you have a race condition.

## Conclusion
- lexical scope

In a lexically scoped language, the scope of an symbol(identifier) is fixed at compile time to some region in the source code containing the symbol's declaration. This means that an symbol is only accessible within that region (including procedures and functions declared within it).

LexicalScope has several advantages
1. it allows for LexicalClosure. (Some languages don't support, like *C*)
2. it allows for compilation of functions to efficient, reentrant machine code

- dynamic scope

This contrasts with dynamic scope where the scope depends on the nesting of procedure and function calls at run time.

Lexical resolution can be determined at compile time, and is also known as *early binding*, while dynamic resolution can in general only be determined at run time, and thus is known as *late binding*.

## Deep Binding vs. Shallow Binding
The two implementation of **DynamicScoping**.
### Deep Binding
To find an identifier's value, the program could traverse the runtime stack, checking each activation record (each function's stack frame) for a value for the identifier. In practice, this is made more efficient via the use of an *association list*, which is a stack of name/value pairs. Pairs are pushed onto this stack whenever declarations are made, and popped whenever variables go out of scope.
### Shallow Binding
Shallow binding is an alternative strategy that is considerably faster, making use of a central reference table, which associates each name with its own stack of meanings. When a variable is rebound, the current value is pushed onto the runtime stack. When a binding exits scope, the old value is popped off of the runtime stack. This avoids a linear search during run-time to find a particular name, but care should be taken to properly maintain this table.

Note that both of these strategies assume a last-in-first-out (LIFO) ordering to bindings for any one variable; in practice all bindings are so ordered.
