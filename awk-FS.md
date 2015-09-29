# Change Field Separator By FS
Script file change-fs.sh
```
#!/bin/awk -f
{
	print $2
	FS=":"
	print $2
}
```
Run above code as:

``` echo "One Two:Three:4 Five" | ./change-fs```

What would be printed?

The script would print out "Two:Three:4" twice. If you deleted the first print statement, it would print out "Three" once!

Why?
## Explanation
- Awk program follows the form:
```pattern {action}```

  Default pattern is null or blank, that matches every line.

  Begin pattern is before any lines are read.

  End pattern is after the last line is read.

- Code in change-fs.sh read line before set FS, and set the variable $2's value. After that, even change FS, $2's value will not change.

- So, we can do it like this:
  * In old awk version
  ```
  #!/bin/awk -f
  {
  	if ( $0 ~ /:/ ) {
  		FS=":";
  	} else {
  		FS=" ";
  	}
  	#print the third field, whatever format
  	print $3
  }
  ```
  * In new awk version
  ```
  #!/bin/awk -f
  {
  	if ( $0 ~ /:/ ) {
  		FS=":";
  		$0=$0
  	} else {
  		FS=" ";
  		$0=$0
  	}
  	#print the third field, whatever format
  	print $3
  }
  ```
  <p style='color:#FF4500'>
  Note: Both of above may not work in some os system or bash shell environment. I am not sure the reason is about os or bash shell.
  </p>
