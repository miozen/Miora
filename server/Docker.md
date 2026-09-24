# Docker运行教程

## 环境变量说明

后端主要使用下面几个变量：

```env
PORT=9003
DB_INFO=127.0.0.1:3306/ThriveX
DB_USERNAME=thrive
DB_PASSWORD=ThriveX@123?
```

### PORT

后端服务端口。

默认：

```env
PORT=9003
```

### DB_INFO

数据库连接地址。

格式：

```env
数据库地址:数据库端口/数据库名
```

如果 MySQL 在宿主机，Docker Desktop 使用：

```env
DB_INFO=host.docker.internal:3306/ThriveX
```

如果 MySQL 也是 Docker 容器，使用 MySQL 容器名：

```env
DB_INFO=mysql:3306/ThriveX
```

注意：容器里的 `127.0.0.1` 指后端容器自己，不是宿主机。

### DB_USERNAME

数据库账号。

```env
DB_USERNAME=thrive
```

### DB_PASSWORD

数据库密码。

```env
DB_PASSWORD=你的数据库密码
```

## 构建镜像

```powershell
docker build -t thrivex-server .
```

## 启动容器

```powershell
docker run -d --name thrivex-server -p 9003:9003 -e DB_INFO=你的数据库地址:3306/ThriveX -e DB_USERNAME=你的数据库账号 -e DB_PASSWORD=你的数据库密码 thrivex-server
```

示例：

```powershell
docker run -d --name thrivex-server -p 9003:9003 -e DB_INFO=host.docker.internal:3306/ThriveX -e DB_USERNAME=thrive -e DB_PASSWORD=你的数据库密码 thrivex-server
```

说明：

- `-d`：后台运行
- `--name thrivex-server`：容器名称
- `-p 9003:9003`：本机9003端口映射到容器9003端口
- `-e`：传入环境变量
- `thrivex-server`：镜像名称

## 访问接口

本地访问：

```text
http://localhost:9003
```

服务器直接访问：

```text
http://服务器IP:9003
```

域名访问：

```text
https://你的后端域名
```

## 查看日志

```powershell
docker logs -f thrivex-server
```

## 重新构建并启动

```powershell
docker rm -f thrivex-server
docker build -t thrivex-server .
docker run -d --name thrivex-server -p 9003:9003 -e DB_INFO=你的数据库地址:3306/ThriveX -e DB_USERNAME=你的数据库账号 -e DB_PASSWORD=你的数据库密码 thrivex-server
```
