当定义thrift service的方法时候，参数可以指定数字id，也可以不指定。这两者生成的代码是不一定的：
1. 指定field id：
    ```
    service MisearchWeiboDomainService {
        string process(1: string name, 2: i64 code);
    }
    ```
    生成的参数处理代码（python）如下：
    ```
    class process_args:
      """
      Attributes:
       - name
       - code
      """
      thrift_spec = (
        None, # 0
        (1, TType.STRING, 'name', None, None, ), # 1
        (2, TType.I64, 'code', None, None, ), # 2
      )
      def __init__(self, name=None, code=None,):
        self.name = name
        self.code = code
      def read(self, iprot):
        if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
          fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
          return
        iprot.readStructBegin()
        while True:
          (fname, ftype, fid) = iprot.readFieldBegin()
          if ftype == TType.STOP:
            break
          if fid == 1:
            if ftype == TType.STRING:
              self.name = iprot.readString();
            else:
              iprot.skip(ftype)
          elif fid == 2:
            if ftype == TType.I64:
              self.code = iprot.readI64();
            else:
              iprot.skip(ftype)
          else:
            iprot.skip(ftype)
          iprot.readFieldEnd()
        iprot.readStructEnd()
      def write(self, oprot):
        if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
          oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
          return
        oprot.writeStructBegin('process_args')
        if self.name != None:
          oprot.writeFieldBegin('name', TType.STRING, 1)
          oprot.writeString(self.name)
          oprot.writeFieldEnd()
        if self.code != None:
          oprot.writeFieldBegin('code', TType.I64, 2)
          oprot.writeI64(self.code)
          oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()
        def validate(self):
          return
    ```
2. 如果未指定field id：
    ```
    service TestService {
        string process(string name, i64 code);
    }
    ```
    生成：
    首先会有警告信息：
    ```
    [WARNING:/home/mi/mines/demos/thrift_service/thrift/test.thrift:2] No field key specified for name, resulting protocol may have conflicts or not be backwards compatible!

    [WARNING:/home/mi/mines/demos/thrift_service/thrift/test.thrift:2] No field key specified for code, resulting protocol may have conflicts or not be backwards compatible!

    [WARNING:/home/mi/mines/demos/thrift_service/thrift/test.thrift:2] No field key specified for name, resulting protocol may have conflicts or not be backwards compatible!

    [WARNING:/home/mi/mines/demos/thrift_service/thrift/test.thrift:2] No field key specified for code, resulting protocol may have conflicts or not be backwards compatible!
    ```

    生成的参数处理代码（python）如下：
    ```
    class process_args:
      """
      Attributes:
       - name
       - code
      """

      thrift_spec = None
      def __init__(self, name=None, code=None,):
        self.name = name
        self.code = code

      def read(self, iprot):
        if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
          fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
          return
        iprot.readStructBegin()
        while True:
          (fname, ftype, fid) = iprot.readFieldBegin()
          if ftype == TType.STOP:
            break
          if fid == -1:
            if ftype == TType.STRING:
              self.name = iprot.readString();
            else:
              iprot.skip(ftype)
          elif fid == -2:
            if ftype == TType.I64:
              self.code = iprot.readI64();
            else:
              iprot.skip(ftype)
          else:
            iprot.skip(ftype)
          iprot.readFieldEnd()
        iprot.readStructEnd()

      def write(self, oprot):
        if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
          oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
          return
        oprot.writeStructBegin('process_args')
        if self.code != None:
          oprot.writeFieldBegin('code', TType.I64, -2)
          oprot.writeI64(self.code)
          oprot.writeFieldEnd()
        if self.name != None:
          oprot.writeFieldBegin('name', TType.STRING, -1)
          oprot.writeString(self.name)
          oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()
        def validate(self):
          return`

    ```


可见不管指定不指定field id，实际都会有fid，如果没有指定，则从-1开始递减。
这样的设计，可以使得后续需要指定新的field id了，指定和未指定的field能够兼容。 但是如果删除旧的参数，那么就会造成不兼容，所以生成的时候，会有警告。因此，指定field id是个好习惯。