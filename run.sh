#!/bin/sh

../../play/play run --%prod -server -XX:+OptimizeStringConcat -XX:-RelaxAccessControlCheck -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -XX:CompileThreshold=200 -XX:+CMSClassUnloadingEnabled
