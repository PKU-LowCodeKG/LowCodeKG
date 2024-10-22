# 低代码知识图谱软件项目

面向低代码开发的知识图谱构建及检索服务

## 环境配置
JDK 17 \
Spring Boot 3.3.4 \
Maven 3.9.9 \
Neo4j 5.1.0 \
ES

### neo4j配置
使用docker配置，neo4j版本为5.24

请提前准备一个文件夹{your_path}，用来挂载容器内的目录
```
docker pull neo4j:5.24

docker run -d -p 7474:7474 -p 7687:7687 --name neo4j-5.24 -e "NEO4J_AUTH=neo4j/neo4j123456" -v {your_path}/data:/data -v {your_path}/logs:/logs -v {your_path}/conf:/var/lib/neo4j/conf -v {your_path}/import:/var/lib/neo4j/import neo4j:5.24
```

## 项目结构
- controller: 前端请求处理，调用服务接口并返回
- dao: 数据访问层，提供 Neo4j 和 ES 的数据存取接口
- extraction: 知识挖掘及关联的插件集
- schema: 定义知识图谱的元模型，包括实体、关系、属性等。通过调用 dao 层接口实现数据持久化
- search: 组件及模板检索的接口定义及实现，基于 dao 层接口调用，向 controller 提供检索服务


## 知识图谱 Schema
![img.png](src/main/resources/static/img.png)

## 数据源
- [蚂蚁开源组件库](https://github.com/ant-design/ant-design)