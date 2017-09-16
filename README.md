# gdns

gdns 是一个被设计用于防污染的 DNS 转发器 (forwarder)，它将所有收到的 DNS 请求通过 TLS 安全连接到后端 Google 递归 DNS 服务器，确保了 DNS 数据的安全性；与此同时，它会根据客户端的 IP 地址数据 (edns_subnet) 查询对应的最优结果。

gdns 也可通过配置文件手动设置某些域名的解析结果，如果一些域名正常解析出来的 IP 无法使用，你可以使用配置文件手动调正这些域名的 IP。配置文件中可通过正则表达式将符合某规则的域名指向同一个特定的 IP。

## Quick Start

在命令行中执行以下指令即可启动 gdns 服务器：

```shell
$ java gdns.jar -p 53
```

> 值得注意的是，你需要确保你的服务器能够正常访问 https://dns.google.com 该程序才能正确运行。

## Todo List
