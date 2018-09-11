#!/bin/sh
base=$(cd `dirname $0`; pwd)
cd ${base}
# 每秒发送的DataPoint数据
export TPS=50000
# TAG数量
export TAG_NUMBER=8
# 每个请求中包含的DataPoint数量
export BATCH_NUMBER=400
# 发送请求的线程数量
export THREADS=8
# OpenTSDB的IP地址
export ADDRESS=8.5.213.12
# OpenTSDB的端口
export PORT=4242
# 测试运行时间
export RUN_TIME=10000
# 是否同步写入HBase
export SYNC=true
# 模拟数据的开始时间
export START_DATE="2000-01-01 00:00:00"
# JAVA运行命令
java -Xmx3G -cp .:tsdbperftool.jar enn.platform.opentsdbtest.TSDBPerfTool