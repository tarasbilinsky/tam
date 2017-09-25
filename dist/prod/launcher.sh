#!/usr/bin/env bash

#hostip=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
./bin/tam -J-Xmx5000M #-Dakka.remote.netty.tcp.hostname=$hostip -Dakka.remote.netty.tcp.bind-hostname=172.17.0.2

