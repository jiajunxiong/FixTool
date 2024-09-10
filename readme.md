### README
#### gradle, java-jdk, junit,

##### how to run
1. ./gradlew clean
2. ./gradlew build
3. ./gradlew run


Example Output:
```
SingleThreaded average parsing time: 0.001736 ms/op
MultiThreaded average parsing time: 0.001837 ms/op
Parsed Message: {BeginString=FIX.4.2, SenderCompID=CLIENT12, Symbol=IBM, TargetCompID=BROKER12, NoStrategyParameters=[{StrategyParameterName=ExecutionStyle, StrategyParameterType=String, StrategyParameterValue=Aggressive}, {StrategyParameterName=ExecutionStyle, StrategyParameterType=String, StrategyParameterValue=Passive}], ClOrdID=123456, MsgType=D, MsgSeqNum=215, TransactTime=20230905-09:30:00, Side=1, SendingTime=20230905-09:30:00, CheckSum=003, TimeInForce=0, BodyLength=112}
```