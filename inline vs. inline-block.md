# inline vs. inline-block
## Element displayed as block only can be set width, height and other block styles

You cannot set height and width for elements with ```display:inline;```, need ```display:inline-block;``` instead.

## Space Between Inline Elements
layout-code1:
```
<div>
  <a href="#">One</a>
  <a href="#">Two</a>
  <a href="#">Three</a>
</div>
```
layout-code2:
```
<div>
  <a href="#">One</a
  ><a href="#">Two</a
  ><a href="#">Three</a>
</div>
```
The result of layout-code1 will display three elements separating by space. But  layout-code2 will display without space separation.

It is obvious to ```<a></a>```. But also to any other inline elements it will work as the same. For example:
```
<div>
  <div class="item">One</div>
  <div class="item">Two</div>
  <div class="item">Three</div>
</div>

.item{
  display: inline;
  background: green;
}
```
### Solution to remove space between inline elements

- As above, remove spaces between inline elements

- Using negative margin *(But the value of margin can not be set precisely.)*

```
<div>
  <div class="item">One</div>
  <div class="item">Two</div>
  <div class="item">Three</div>
</div>

.item{
  display: inline;
  background: green;
  margin-left: -0.3em;
}

```

- Skip the closing tag in **HTML5**

```
<div>
  <div id="div1" class="item">One
  <div class="item">Two
  <div class="item">Three
</div>

.item{
  display: inline;
  background: red;
  border: 1px solid green;
}
```

- Set font size to zero

```
div{
    font-size: 0px;
}

.item{
  display: inline;
  background: red;
  border: 1px solid green;
  font-size: 20px;
}
```

- Float inline elements

```
.item{
  display: inline;
  background: red;
  border: 1px solid green;
  float: left;
}
```

- Using flex if supported

```
<div>
  <a id="div1" class="item">One</a>
    <a class="item">Two</a>
    <a class="item">Three</a>
</div>

.item{
  display: flex;   
  background: red;
  border: 1px solid green;
  width: 100px;
}
```
But using div instead of a, flex box display differently.
