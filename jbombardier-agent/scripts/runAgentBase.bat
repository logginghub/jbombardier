pushd %~dp0
@echo off
set classpath=
set classpath=%classpath%;target\classes
set classpath=%classpath%;..\jbombardier-common\target\classes
set classpath=%classpath%;..\vl-utils\target\classes
set classpath=%classpath%;..\vl-messaging2\target\classes
rem set classpath=%classpath%;..\vl-logging\target\classes
set classpath=%classpath%;..\vl-logging-client\target\classes
rem set classpath=%classpath%;..\vl-utils-java6\target\classes
rem set classpath=%classpath%;..\vl-analytics\target\classes

set classpath=%classpath%;D:\Development\mavenRepository\org\slf4j\slf4j-api\1.6.4\slf4j-api-1.6.4.jar
set classpath=%classpath%;D:\Development\mavenRepository\org\slf4j\slf4j-log4j12\1.6.3\slf4j-log4j12-1.6.3.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\com\miglayout\miglayout\3.7.2\miglayout-3.7.2.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\commons-lang\commons-lang\2.5\commons-lang-2.5.jar
set classpath=%classpath%;D:\Development\mavenRepository\kryo\kryonet\2.12\kryonet-2.12.jar
set classpath=%classpath%;D:\Development\mavenRepository\backport-util-concurrent\backport-util-concurrent\3.1\backport-util-concurrent-3.1.jar
set classpath=%classpath%;D:\Development\mavenRepository\org\fusesource\sigar\1.6.4\sigar-1.6.4.jar;
set classpath=%classpath%;D:\Development\mavenRepository\org\fusesource\sigar-native\1.6.4\sigar-native-1.6.4.jar
set classpath=%classpath%;D:\Development\mavenRepository\log4j\log4j\1.2.14\log4j-1.2.14.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\jfree\jfreechart\1.0.13\jfreechart-1.0.13.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\jfree\jcommon\1.0.15\jcommon-1.0.15.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\net\sf\opencsv\opencsv\2.0\opencsv-2.0.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\org\simpleframework\simple-xml\2.4.1\simple-xml-2.4.1.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\stax\stax-api\1.0.1\stax-api-1.0.1.jar
rem set classpath=%classpath%;D:\Development\mavenRepository\stax\stax\1.2.0\stax-1.2.0.jar

"C:\Program Files (x86)\Java\jdk1.6.0_20\bin\java.exe"  -classpath %classpath% -Dkryo.objectbuffersize=20000000 -Dkryo.writebuffersize=20000000 com.jbombardier.agent.Agent2 %1

call runAgent%2.bat

