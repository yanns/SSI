#!/bin/sh

../../play/play run --%prod -server -Xms1024m -Xmx1024m -XX:MaxPermSize=128m -XX:+OptimizeStringConcat -XX:-RelaxAccessControlCheck -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:CompileThreshold=200 -XX:+CMSClassUnloadingEnabled
