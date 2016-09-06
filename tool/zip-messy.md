Zip 压缩包，从Window到Unix的编码问题，导致的文件名乱解决方法：

7z方案
需要安装p7zip和convmv，在Fedora下的命令是

su -c 'yum install p7zip convmv'
在ubuntu下的安装命令是

sudo apt-get install p7zip convmv
安装完之后，就可以用7za和convmv两个命令完成解压缩任务。

LANG=C 7za x your-zip-file.zip
convmv -f GBK -t utf8 --notest -r .
第一条命令用于解压缩，而LANG=C表示以US-ASCII这样的编码输出文件名，如果没有这个语言设置，它同样会输出乱码，只不过是UTF8格式的乱码(convmv会忽略这样的乱码)。
第二条命令是将GBK编码的文件名转化为UTF8编码，-r表示递归访问目录，即对当前目录中所有文件进行转换。
