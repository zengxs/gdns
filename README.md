# STUPIDNS
A Stupid DNS Server based on Google Public DNS Service

## configuration
configuration file name is `config.toml`. 
Example:
```toml
[server]
address = "0.0.0.0"
port = 53

[address]
"static.example.com" = { type = "static", address = "127.0.0.1" } # 此域名解析到 address
"redirect.example.com" = { type = "redirect", address = "127.0.0.1" } # 此域下所有域名解析到 address
```
