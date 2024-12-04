---
title: aurora blog 后端需求
date: 2024-11-30
categories:
  - 打黑工
tags:
  - Aurora Blog
---

## 1. 用户信息模块

- Controller: `UserInfoController`
- Service: 
  - `UserInfoService` <= `UserInfoServiceImpl`
  - `TokenService` <= `TokenServiceImpl`
  - `RedisService` <= `RedisServiceImpl`
  - `UserRoleService` <= `UserRoleServiceImpl`
- Mapper:
  - `UserInfoMapper`
  - `UserAuthMapper`

用户信息模块负责管理用户的基本信息、头像、邮箱绑定、订阅状态、角色、禁用状态等功能。管理员可以管理在线用户、修改用户角色及禁用状态，普通用户则可以修改个人信息、头像、邮箱等。使用 MyBatis-Plus 进行数据持久化，使用 Redis 进行缓存管理。

### 1.1 更新用户信息

- **功能描述**：用户可以更新其个人信息（昵称、个人介绍、网站）
- **请求路径**：`PUT /users/info`
- **请求参数**：
  - `UserInfoVO`（请求体，昵称、个人介绍、网站）
- **返回结果**：`ResultVO`
- **实现函数**：
  - `UserInfoController.updateUserInfo`
  - `UserInfoService.updateUserInfo` <= `UserInfoServiceImpl.updateUserInfo`
  - `UserInfoMapper.updateById`

### 1.2 更新用户头像

- **功能描述** : 用户可以上传新的头像
- **请求路径** : `POST /users/avatar`
- **请求参数** : 
  - `MultipartFile file`（上传的头像文件）
- **返回结果** : `ResultVO<String>`（上传后的头像路径）
- **实现函数**：
  - `UserInfoController.updateUserAvatar`
  - `UserInfoService.updateUserAvatar`<= `UserInfoServiceImpl.updateUserAvatar`
  - `UserInfoMapper.updateById`

### 1.3 绑定用户邮箱

- **功能描述** : 用户可以绑定或更新其邮箱地址，通过验证码进行验证。
- **请求路径** : `PUT /users/email`
- **请求参数** : 
  - `EmailVO`（请求体，邮箱、验证码）
- **返回结果** : `ResultVO`
- **实现函数**：
  - `UserInfoController.saveUserEmail`
  - `UserInfoService.saveUserEmail`<= `UserInfoServiceImpl.saveUserEmail`
  - `UserInfoMapper.updateById`

### 1.4 修改用户的订阅状态

- **功能描述** : 用户可以修改其订阅状态。
- **请求路径** : `PUT /users/subscribe`
- **请求参数** : 
  - `SubscribeVO`（请求体，用户ID、订阅状态）
- **返回结果** : `ResultVO`
- **实现函数**：
  - `UserInfoController.updateUserSubscribe`
  - `UserInfoService.updateUserSubscribe`<= `UserInfoServiceImpl.updateUserSubscribe`
  - `UserInfoMapper.updateById`

### 1.5 修改用户角色

- **功能描述** : 管理员可以修改用户角色。
- **请求路径** : `PUT /admin/users/role`
- **请求参数** : 
  - `UserRoleVO`（请求体，用户ID，昵称，角色ID列表）
- **返回结果** : `ResultVO`
- **实现函数**：
  - `UserInfoController.updateUserRole`
  - `UserInfoService.updateUserRole`<= `UserInfoServiceImpl.updateUserRole`
  - `UserInfoMapper.updateById`
  - `UserRoleServiceImpl.remove`
  - `UserRoleServiceImpl.saveBatch`

### 1.6 修改用户禁用状态

- **功能描述** : 管理员可以禁用或启用用户。
- **请求路径** : `PUT /admin/users/disable`
- **请求参数** : 
  - `UserDisableVO`（请求体，用户ID，禁用状态）
- **返回结果** : `ResultVO`
- **实现函数**：
  - `UserInfoController.updateUserDisable`
  - `UserInfoService.updateUserDisable`<= `UserInfoServiceImpl.updateUserDisable`
  - `UserInfoService.removeOnlineUser` <= `UserInfoServiceImpl.removeOnlineUser`

### 1.7 查看在线用户

- **功能描述** : 管理员可以查看当前在线用户列表。
- **请求路径** : `GET /admin/users/online`
- **请求参数** : 
  - `ConditionVO`（请求体，一些筛选条件）
- **返回结果** : `ResultVO<PageResultDTO<UserOnlineDTO>>`（分页返回在线用户列表）
- **实现函数**：
  - `UserInfoController.listOnlineUsers`
  - `UserInfoService.listOnlineUsers`<= `UserInfoServiceImpl.listOnlineUsers`
  - `RedisService.hGetAll` <= `RedisServiceImpl.hGetAll`

### 1.8 下线用户

- **功能描述** : 管理员可以将指定用户下线。
- **请求路径** : `DELETE /admin/users/{userInfoId}/online`
- **请求参数** : 
  - `userInfoId`（URL路径参数Integer，用户ID）
- **返回结果** : `ResultVO`
- **实现函数**：
  - `UserInfoController.removeOnlineUser`
  - `UserInfoService.removeOnlineUser`<= `UserInfoServiceImpl.removeOnlineUser`
  - `userAuthMapper.selectOne`
  - `TokenService.delLoginUser` <= `TokenServiceImpl.delLoginUser`

### 1.9 根据id获取用户信息

- **功能描述** : 获取指定用户的详细信息。
- **请求路径** : `GET /users/info/{userInfoId}`
- **请求参数** : 
  - `userInfoId`（URL路径参数Integer，用户ID）
- **返回结果** : `ResultVO<UserInfoDTO>`
- **实现函数**：
  - `UserInfoController.getUserInfoById`
  - `UserInfoService.getUserInfoById`<= `UserInfoServiceImpl.getUserInfoById`
  - `UserInfoMapper.selectById`

---

***<u>以下内容包含AI辅助创作</u>，但已经进行了大量人工检验和修改*   : -)**

## 2. 用户账号模块 

- Controller: `UserAuthController`
- Service: 
  - `UserAuthService` <= `UserAuthServiceImpl`
  - `RedisService` <= `RedisServiceImpl`
  - `AuroraInfoService` <= `AuroraInfoServiceImpl`
  - `TokenService` <= `TokenServiceImpl`
  - `SocialLoginStrategyContext`
- Mapper: 
  - `UserAuthMapper`
  - `UserInfoMapper`
  - `UserRoleMapper`

用户账号模块负责用户的注册、登录、修改密码、邮箱验证码等功能，并提供管理员功能，包括查询用户信息、修改管理员密码、获取用户区域分布等。使用 MyBatis-Plus 进行数据持久化，使用 Redis 进行缓存管理，RabbitMQ 用于邮件发送。

### 2.1 发送邮箱验证码 

- **功能描述** ：用户可以请求发送邮箱验证码，用于注册、修改密码等操作。
- **请求路径** ：`GET /users/code`
- **请求参数** ： 
  - `username`（参数String）
- **返回结果** ：`ResultVO<?>`（操作结果）
- **实现函数** ： 
  - `UserAuthController.sendCode`
  - `UserAuthService.sendCode` <= `UserAuthServiceImpl.sendCode`
  - `RedisService.set` <= `RedisServiceImpl.set`

### 2.2 获取用户区域分布 

- **功能描述** ：管理员可以查询用户的区域分布。
- **请求路径** ：`GET /admin/users/area`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含查询条件，如区域类型等）
- **返回结果** ：`ResultVO<List<UserAreaDTO>>`（用户区域信息列表）
- **实现函数** ： 
  - `UserAuthController.listUserAreas`
  - `UserAuthService.listUserAreas` <= `UserAuthServiceImpl.listUserAreas`
  - `RedisService.get` <= `RedisServiceImpl.get`
  - `RedisService.hGetAll` <= `RedisServiceImpl.hGetAll`

### 2.3 查询后台用户列表 

- **功能描述** ：管理员可以查询后台的用户列表，支持分页和筛选条件。
- **请求路径** ：`GET /admin/users`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含分页信息和筛选条件）
- **返回结果** ：`ResultVO<PageResultDTO<UserAdminDTO>>`（用户列表分页数据）
- **实现函数** ： 
  - `UserAuthController.listUsers`
  - `UserAuthService.listUsers` <= `UserAuthServiceImpl.listUsers`
  - `UserAuthMapper.countUser`
  - `UserAuthMapper.listUsers`

### 2.4 用户注册 

- **功能描述** ：用户通过邮箱和验证码进行注册。
- **请求路径** ：`POST /users/register`
- **请求参数** ： 
  - `UserVO`（请求体，包含邮箱、密码等信息）
- **返回结果** ：`ResultVO<?>`（操作结果）
- **实现函数** ： 
  - `UserAuthController.register`
  - `UserAuthService.register` <= `UserAuthServiceImpl.register`
  - `AuroraInfoService.getWebsiteConfig().getUserAvatar()` <= `AuroraInfoServiceImpl.getWebsiteConfig()`
  - `UserInfoMapper.insert`
  - `UserRoleMapper.insert`
  - `UserAuthMapper.insert`

### 2.5 修改密码 

- **功能描述** ：用户可以修改自己的密码。
- **请求路径** ：`PUT /users/password`
- **请求参数** ： 
  - `UserVO`（请求体，包含邮箱和新密码）
- **返回结果** ：`ResultVO<?>`（操作结果）
- **实现函数** ： 
  - `UserAuthController.updatePassword`
  - `UserAuthService.updatePassword` <= `UserAuthServiceImpl.updatePassword`
  - `UserAuthMapper.update`

### 2.6 修改管理员密码 

- **功能描述** ：管理员可以修改自己的登录密码。
- **请求路径** ：`PUT /admin/users/password`
- **请求参数** ： 
  - `PasswordVO`（请求体，包含旧密码和新密码）
- **返回结果** ：`ResultVO<?>`（操作结果）
- **实现函数** ： 
  - `UserAuthController.updateAdminPassword`
  - `UserAuthService.updateAdminPassword` <= `UserAuthServiceImpl.updateAdminPassword`
  - `UserAuthMapper.updateById`

### 2.7 用户登出 

- **功能描述** ：用户登出并清除登录状态。
- **请求路径** ：`POST /users/logout`
- **请求参数** ：无
- **返回结果** ：`ResultVO<UserLogoutStatusDTO>`（登出状态）
- **实现函数** ： 
  - `UserAuthController.logout`
  - `UserAuthService.logout` <= `UserAuthServiceImpl.logout`
  - `TokenService.delLoginUser` <= `TokenServiceImpl.delLoginUser`

### 2.8 QQ 登录 

- **功能描述** ：用户通过 QQ 账号进行登录。
- **请求路径** ：`POST /users/oauth/qq`
- **请求参数** ： 
  - `QQLoginVO`（请求体，包含 QQ 登录相关信息）
- **返回结果** ：`ResultVO<UserInfoDTO>`（用户信息）
- **实现函数** ： 
  - `UserAuthController.qqLogin`
  - `UserAuthService.qqLogin` <= `UserAuthServiceImpl.qqLogin`
  - `SocialLoginStrategyContext.executeLoginStrategy`

## 3. 说说模块

- Controller: `TalkController`
- Service: 
  - `TalkService` <= `TalkServiceImpl`
  - `UploadStrategyContext`
- Mapper: 
  - `TalkMapper`
  - `CommentMapper`

说说模块用于处理用户发布、查看、修改、删除说说的功能，包括前台的说说列表和后台管理功能。同时支持图片上传，并集成了评论计数功能。

### 3.1 查看说说列表 

- **功能描述** ：获取所有公开的说说列表。
- **请求路径** ：`GET /talks`
- **请求参数** ：无
- **返回结果** ： `ResultVO<PageResultDTO<TalkDTO>>`（分页返回说说列表，包含说说详细信息）
- **实现函数** ： 
  - `TalkController.listTalks`
  - `TalkService.listTalks` <= `TalkServiceImpl.listTalks`
  - `TalkMapper.selectCount`
  - `TalkMapper.listTalks`
  - `CommentMapper.listCommentCountByTypeAndTopicIds`

### 3.2 根据id查看说说 

- **功能描述** ：根据说说 ID 获取说说详细内容。
- **请求路径** ：`GET /talks/{talkId}`
- **请求参数** ： 
  - `talkId`（URL路径参数Integer，说说 ID）
- **返回结果** ： `ResultVO<TalkDTO>`（包含说说的详细信息）
- **实现函数** ： 
  - `TalkController.getTalkById`
  - `TalkService.getTalkById` <= `TalkServiceImpl.getTalkById`
  - `TalkMapper.getTalkById`
  - `CommentMapper.listCommentCountByTypeAndTopicId`

### 3.3 上传说说图片 

- **功能描述** ：上传说说的图片。
- **请求路径** ：`POST /admin/talks/images`
- **请求参数** ： 
  - `MultipartFile file`（上传的图片文件）
- **返回结果** ：`ResultVO<String>`（图片上传后的文件路径）
- **实现函数** ： 
  - `TalkController.saveTalkImages`
  - `UploadStrategyContext.executeUploadStrategy`

### 3.4 保存或修改说说 

- **功能描述** ：保存或修改说说内容。
- **请求路径** ：`POST /admin/talks`
- **请求参数** ：
  - `TalkVO` （请求体，包含说说的相关信息）
- **返回结果** ：`ResultVO`
- **实现函数** ：
  - `TalkController.saveOrUpdateTalk`
  - `TalkService.saveOrUpdateTalk` <= `TalkServiceImpl.saveOrUpdateTalk`
  - `TalkService.saveOrUpdate`

### 3.5 删除说说 

- **功能描述** ：删除指定的说说。
- **请求路径** ：`DELETE /admin/talks`
- **请求参数** ： 
  - `talkIds`：要删除的说说 ID 列表，类型为 `List<Integer>`
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `TalkController.deleteTalks`
  - `TalkService.deleteTalks` <= `TalkServiceImpl.deleteTalks`
  - `TalkMapper.deleteBatchIds`

### 3.6 查看后台说说 

- **功能描述** ：后台管理员查看说说列表，支持按条件筛选。
- **请求路径** ：`GET /admin/talks`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含筛选条件）
- **返回结果** ：`ResultVO<PageResultDTO<TalkAdminDTO>>`（包含后台说说列表和分页信息）
- **实现函数** ： 
  - `TalkController.listBackTalks`
  - `TalkService.listBackTalks` <= `TalkServiceImpl.listBackTalks`
  - `TalkMapper.selectCount`
  - `TalkMapper.listTalksAdmin`

### 3.7 根据id查看后台说说 

- **功能描述** ：后台管理员根据说说 ID 获取说说详细信息。
- **请求路径** ：`GET /admin/talks/{talkId}`
- **请求参数** ： 
  - `talkId`（URL路径参数Integer，说说 ID）
- **返回结果** ： `ResultVO<TalkAdminDTO>`（包含后台管理说说的详细信息）
- **实现函数** ： 
  - `TalkController.getBackTalkById`
  - `TalkService.getBackTalkById` <= `TalkServiceImpl.getBackTalkById`
  - `TalkMapper.getTalkByIdAdmin`

## 4. 标签模块 

- Controller: `TagController`
- Service: 
  - `TagService` <= `TagServiceImpl`
- Mapper: 
  - `TagMapper`
  - `ArticleTagMapper`

标签模块用于处理标签的增删查改（CRUD）操作，包括前台获取标签列表、后台标签管理和标签的搜索、添加、修改、删除功能。

### 4.1 获取所有标签 

- **功能描述** ：获取所有标签。
- **请求路径** ：`GET /tags/all`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<TagDTO>>`，包含所有标签的列表。
- **实现函数** ： 
  - `TagController.getAllTags`
  - `TagService.listTags` <= `TagServiceImpl.listTags`
  - `TagMapper.listTags()`

### 4.2 获取前十个标签 

- **功能描述** ：获取前十个热门标签。
- **请求路径** ：`GET /tags/topTen`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<TagDTO>>`，包含前十个标签的列表。
- **实现函数** ： 
  - `TagController.getTopTenTags`
  - `TagService.listTopTenTags` <= `TagServiceImpl.listTopTenTags`
  - `TagMapper.listTopTenTags`

### 4.3 查询后台标签列表 

- **功能描述** ：根据查询条件获取后台标签列表，支持分页。
- **请求路径** ：`GET /admin/tags`
- **请求参数** ： 
  - `conditionVO`：查询条件，类型为 `ConditionVO`，支持分页和关键词搜索。
- **返回结果** ： `ResultVO<PageResultDTO<TagAdminDTO>>`，包含标签列表和分页信息。
- **实现函数** ： 
  - `TagController.listTagsAdmin`
  - `TagService.listTagsAdmin` <= `TagServiceImpl.listTagsAdmin`
  - `TagMapper.selectCount`
  - `TagMapper.listTagsAdmin`

### 4.4 搜索文章标签 

- **功能描述** ：根据关键词搜索后台标签。
- **请求路径** ：`GET /admin/tags/search`
- **请求参数** ： 
  - `conditionVO`：查询条件，类型为 `ConditionVO`，支持关键词搜索。
- **返回结果** ： `ResultVO<List<TagAdminDTO>>`，包含符合搜索条件的标签列表。
- **实现函数** ： 
  - `TagController.listTagsAdminBySearch`
  - `TagService.listTagsAdminBySearch` <= `TagServiceImpl.listTagsAdminBySearch`
  - `TagMapper.selectList`

### 4.5 添加或修改标签 

- **功能描述** ：添加或修改标签信息。
- **请求路径** ：`POST /admin/tags`
- **请求参数** ： 
  - `tagVO`：`TagVO` 对象，包含标签的相关信息。
- **返回结果** ：`ResultVO<?>` 
- **实现函数** ： 
  - `TagController.saveOrUpdateTag`
  - `TagService.saveOrUpdateTag` <= `TagServiceImpl.saveOrUpdateTag`
  - `TagMapper.selectOne`
  - `TagService.saveOrUpdate`

### 4.6 删除标签 

- **功能描述** ：删除指定的标签。
- **请求路径** ：`DELETE /admin/tags`
- **请求参数** ： 
  - `tagIdList`：要删除的标签 ID 列表，类型为 `List<Integer>`
- **返回结果** ：`ResultVO<?>`
- **实现函数** ： 
  - `TagController.deleteTag`
  - `TagService.deleteTag` <= `TagServiceImpl.deleteTag`
  - `ArticleTagMapper.selectCount`
  - `TagMapper.deleteBatchIds`

## 5. 角色模块 

- Controller: `RoleController`
- Service: 
  - `RoleService` <= `RoleServiceImpl`
  - `RoleResourceService` <= `RoleResourceServiceImpl`
  - `RoleMenuService` <= `RoleMenuServiceImpl`
- Mapper: 
  - `RoleMapper`
  - `UserRoleMapper`
  - `RoleMenuMapper`
  - `RoleResourceMapper`

角色模块负责管理系统中的角色信息，包括角色的增删查改（CRUD）操作，角色与用户、菜单、资源的关联管理。管理员可以通过该模块配置和管理用户的角色权限。

### 5.1 查询用户角色选项 

- **功能描述** ：查询系统中所有可分配的角色选项。
- **请求路径** ：`GET /admin/users/role`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<UserRoleDTO>>`，包含所有角色信息。
- **实现函数** ： 
  - `RoleController.listUserRoles`
  - `RoleService.listUserRoles` <= `RoleServiceImpl.listUserRoles`
  - `RoleMapper.selectList`

### 5.2 查询角色列表 

- **功能描述** ：根据查询条件获取角色列表，支持分页和关键词搜索。
- **请求路径** ：`GET /admin/roles`
- **请求参数** ： 
  - `conditionVO`：查询条件，类型为 `ConditionVO`，支持分页和关键词搜索。
- **返回结果** ：`ResultVO<PageResultDTO<RoleDTO>>`，包含角色列表和分页信息。
- **实现函数** ： 
  - `RoleController.listRoles`
  - `RoleService.listRoles` <= `RoleServiceImpl.listRoles`
  - `RoleMapper.selectCount`
  - `RoleMapper.listRoles`

### 5.3 保存或更新角色 

- **功能描述** ：添加或更新角色信息。
- **请求路径** ：`POST /admin/role`
- **请求参数** ： 
  - `roleVO`：`RoleVO` 对象，包含角色的相关信息。
- **返回结果** ：`ResultVO<?>`
- **实现函数** ： 
  - `RoleController.saveOrUpdateRole`
  - `RoleService.saveOrUpdateRole` <= `RoleServiceImpl.saveOrUpdateRole`
  - `RoleMapper.selectOne`
  - `RoleResourceService.saveOrUpdate`
  - `RoleResourceService.remove`
  - `RoleResourceService.saveBatch`
  - `RoleMenuService.remove`
  - `RoleMenuService.saveBatch`

### 5.4 删除角色 

- **功能描述** ：删除指定的角色。如果角色下存在用户，则不能删除。
- **请求路径** ：`DELETE /admin/roles`
- **请求参数** ： 
  - `roleIdList`：要删除的角色 ID 列表，类型为 `List<Integer>`。
- **返回结果** ：`ResultVO<?>`
- **实现函数** ： 
  - `RoleController.deleteRoles`
  - `RoleService.deleteRoles` <= `RoleServiceImpl.deleteRoles`
  - `UserRoleMapper.selectCount`
  - `RoleMapper.deleteBatchIds`

## 6. 资源模块 

- Controller: `ResourceController`
- Service: 
  - `ResourceService` <= `ResourceServiceImpl`
- Mapper: 
  - `ResourceMapper`
  - `RoleResourceMapper`

资源模块负责管理系统中的资源，包括查看、新增、修改、删除资源，以及获取角色资源选项等功能。

### 6.1 查看资源列表 

- **功能描述** ：管理员可以查看资源列表，根据条件过滤资源。
- **请求路径** ：`GET /admin/resources`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含筛选条件，如关键词）
- **返回结果** ：`ResultVO<List<ResourceDTO>>`（资源列表）
- **实现函数** ： 
  - `ResourceController.listResources`
  - `ResourceService.listResources` <= `ResourceServiceImpl.listResources`
  - `ResourceMapper.selectList`
  - `ResourceServiceImpl.listResourceChildren`

### 6.2 删除资源 

- **功能描述** ：管理员可以删除指定的资源，删除时需确保该资源下没有绑定角色。
- **请求路径** ：`DELETE /admin/resources/{resourceId}`
- **请求参数** ： 
  - `resourceId`（URL路径参数Integer，资源ID）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `ResourceController.deleteResource`
  - `ResourceService.deleteResource` <= `ResourceServiceImpl.deleteResource`
  - `RoleResourceMapper.selectCount`
  - `ResourceMapper.deleteBatchIds`

### 6.3 新增或修改资源 

- **功能描述** ：管理员可以新增或修改资源信息。
- **请求路径** ：`POST /admin/resources`
- **请求参数** ： 
  - `ResourceVO`（请求体，包含资源名称、URL等信息）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `ResourceController.saveOrUpdateResource`
  - `ResourceService.saveOrUpdateResource` <= `ResourceServiceImpl.saveOrUpdateResource`
  - `ResourceService.saveOrUpdate`

### 6.4 查看角色资源选项 

- **功能描述** ：管理员可以查看角色资源的选项，用于角色配置资源权限。
- **请求路径** ：`GET /admin/role/resources`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<LabelOptionDTO>>`（角色资源选项列表）
- **实现函数** ： 
  - `ResourceController.listResourceOption`
  - `ResourceService.listResourceOption` <= `ResourceServiceImpl.listResourceOption`
  - `ResourceMapper.selectList`
  - `ResourceServiceImpl.listResourceChildren`

## 7. 照片模块

- Controller: `PhotoController`
- Service: 
  - `PhotoService` <= `PhotoServiceImpl`
  - `PhotoAlbumService` <= `PhotoAlbumServiceImpl`
  - `UploadStrategyContext`
- Mapper: 
  - `PhotoMapper`
  - `PhotoAlbumMapper`

照片模块负责管理系统中的照片信息，包括上传、查看、删除和更新照片等功能。

### 7.1 上传照片 

- **功能描述** ：管理员可以上传照片，照片将存储在指定路径。
- **请求路径** ：`POST /admin/photos/upload`
- **请求参数** ： 
  - `file`（`MultipartFile`，照片文件）
- **返回结果** ：`ResultVO<String>`（上传后的文件路径）
- **实现函数** ： 
  - `PhotoController.savePhotoAlbumCover`
  - `UploadStrategyContext.executeUploadStrategy`

### 7.2 根据相册ID获取照片列表 

- **功能描述** ：管理员可以根据相册ID获取该相册下的所有照片。
- **请求路径** ：`GET /admin/photos`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含筛选条件）
- **返回结果** ：`ResultVO<PageResultDTO<PhotoAdminDTO>>`（分页后的照片列表）
- **实现函数** ： 
  - `PhotoController.listPhotos`
  - `PhotoService.listPhotos` <= `PhotoServiceImpl.listPhotos`
  - `PhotoMapper.selectPage`

### 7.3 更新照片信息 

- **功能描述** ：管理员可以更新照片的信息（如名称、描述等）。
- **请求路径** ：`PUT /admin/photos`
- **请求参数** ： 
  - `PhotoInfoVO`（请求体，包含照片信息）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoController.updatePhoto`
  - `PhotoService.updatePhoto` <= `PhotoServiceImpl.updatePhoto`
  - `PhotoMapper.updateById`

### 7.4 保存照片 

- **功能描述** ：管理员可以保存新上传的照片。
- **请求路径** ：`POST /admin/photos`
- **请求参数** ： 
  - `PhotoVO`（请求体，包含照片URL、相册ID等信息）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoController.savePhotos`
  - `PhotoService.savePhotos` <= `PhotoServiceImpl.savePhotos`
  - `PhotoService.saveBatch`

### 7.5 移动照片相册 

- **功能描述** ：管理员可以将照片从一个相册移动到另一个相册。
- **请求路径** ：`PUT /admin/photos/album`
- **请求参数** ： 
  - `PhotoVO`（请求体，包含照片ID和目标相册ID）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoController.updatePhotosAlbum`
  - `PhotoService.updatePhotosAlbum` <= `PhotoServiceImpl.updatePhotosAlbum`
  - `PhotoService.updateBatchById`

### 7.6 更新照片删除状态 

- **功能描述** ：管理员可以更改照片的删除状态（标记为已删除或恢复）。
- **请求路径** ：`PUT /admin/photos/delete`
- **请求参数** ： 
  - `DeleteVO`（请求体，包含照片ID和删除状态）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoController.updatePhotoDelete`
  - `PhotoService.updatePhotoDelete` <= `PhotoServiceImpl.updatePhotoDelete`
  - `PhotoService.updateBatchById`
  - `PhotoMapper.updateBatchById`
  - `PhotoMapper.selectList`
  - `PhotoAlbumService.updateBatchById`

### 7.7 删除照片 

- **功能描述** ：管理员可以删除指定的照片。
- **请求路径** ：`DELETE /admin/photos`
- **请求参数** ： 
  - `List<Integer> photoIds`（请求体，包含多个照片ID）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoController.deletePhotos`
  - `PhotoService.deletePhotos` <= `PhotoServiceImpl.deletePhotos`
  - `PhotoMapper.deleteBatchIds`

### 7.8 根据相册ID查看照片列表 

- **功能描述** ：根据相册ID查看相册中的照片列表。
- **请求路径** ：`GET /albums/{albumId}/photos`
- **请求参数** ： 
  - `albumId`（URL路径参数Integer，相册ID）
- **返回结果** ：`ResultVO<PhotoDTO>`（照片详情，包含相册封面、名称和照片列表）
- **实现函数** ： 
  - `PhotoController.listPhotosByAlbumId`
  - `PhotoService.listPhotosByAlbumId` <= `PhotoServiceImpl.listPhotosByAlbumId`
  - `PhotoAlbumService.getOne`
  - `PhotoMapper.selectPage`

## 8. 相册模块 

- Controller: `PhotoAlbumController`
- Service: 
  - `PhotoAlbumService` <= `PhotoAlbumServiceImpl`
  - `UploadStrategyContext`
- Mapper: 
  - `PhotoAlbumMapper`
  - `PhotoMapper`

相册模块负责管理系统中的相册信息，包括创建、删除、查询相册等功能。

### 8.1 上传相册封面 

- **功能描述** ：管理员可以上传相册封面，封面图片将存储在指定路径。
- **请求路径** ：`POST /admin/photos/albums/upload`
- **请求参数** ： 
  - `file`（`MultipartFile`，上传的相册封面文件）
- **返回结果** ：`ResultVO<String>`（上传后的文件路径）
- **实现函数** ： 
  - `PhotoAlbumController.savePhotoAlbumCover`
  - `UploadStrategyContext.executeUploadStrategy`

### 8.2 保存或更新相册 

- **功能描述** ：管理员可以保存或更新相册信息，包括相册名称、封面等。
- **请求路径** ：`POST /admin/photos/albums`
- **请求参数** ： 
  - `PhotoAlbumVO`（请求体，包含相册信息）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoAlbumController.saveOrUpdatePhotoAlbum`
  - `PhotoAlbumService.saveOrUpdatePhotoAlbum` <= `PhotoAlbumServiceImpl.saveOrUpdatePhotoAlbum`
  - `PhotoAlbumMapper.selectOne`
  - `PhotoAlbumService.saveOrUpdate`

### 8.3 查看后台相册列表 

- **功能描述** ：管理员可以查看相册列表，并支持分页查询。
- **请求路径** ：`GET /admin/photos/albums`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含相册名称、删除状态等筛选条件）
- **返回结果** ：`ResultVO<PageResultDTO<PhotoAlbumAdminDTO>>`（分页后的相册列表）
- **实现函数** ： 
  - `PhotoAlbumController.listPhotoAlbumBacks`
  - `PhotoAlbumService.listPhotoAlbumsAdmin` <= `PhotoAlbumServiceImpl.listPhotoAlbumsAdmin`
  - `PhotoAlbumMapper.selectCount`
  - `PhotoAlbumMapper.listPhotoAlbumsAdmin`

### 8.4 获取后台相册列表信息 

- **功能描述** ：管理员可以获取所有相册的简要信息。
- **请求路径** ：`GET /admin/photos/albums/info`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<PhotoAlbumDTO>>`（相册简要信息列表）
- **实现函数** ： 
  - `PhotoAlbumController.listPhotoAlbumBackInfos`
  - `PhotoAlbumService.listPhotoAlbumInfosAdmin` <= `PhotoAlbumServiceImpl.listPhotoAlbumInfosAdmin`
  - `PhotoAlbumMapper.selectList`

### 8.5 根据ID获取后台相册信息 

- **功能描述** ：管理员可以通过相册ID查看该相册的详细信息，包括相册名称、封面、照片数量等。
- **请求路径** ：`GET /admin/photos/albums/{albumId}/info`
- **请求参数** ： 
  - `albumId`（URL路径参数，相册ID）
- **返回结果** ：`ResultVO<PhotoAlbumAdminDTO>`（相册详细信息）
- **实现函数** ： 
  - `PhotoAlbumController.getPhotoAlbumBackById`
  - `PhotoAlbumService.getPhotoAlbumByIdAdmin` <= `PhotoAlbumServiceImpl.getPhotoAlbumByIdAdmin`
  - `PhotoAlbumMapper.selectById`
  - `PhotoAlbumMapper.selectCount`

### 8.6 根据ID删除相册 

- **功能描述** ：管理员可以删除指定ID的相册，如果相册下有照片，将会标记为删除状态。
- **请求路径** ：`DELETE /admin/photos/albums/{albumId}`
- **请求参数** ： 
  - `albumId`（URL路径参数，相册ID）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `PhotoAlbumController.deletePhotoAlbumById`
  - `PhotoAlbumService.deletePhotoAlbumById` <= `PhotoAlbumServiceImpl.deletePhotoAlbumById`
  - `PhotoMapper.selectCount`
  - `PhotoAlbumMapper.updateById`
  - `PhotoMapper.update`
  - `PhotoAlbumMapper.deleteById`

### 8.7 获取相册列表 

- **功能描述** ：所有用户可以查看公开状态的相册列表。
- **请求路径** ：`GET /photos/albums`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<PhotoAlbumDTO>>`（公开状态的相册列表）
- **实现函数** ： 
  - `PhotoAlbumController.listPhotoAlbums`
  - `PhotoAlbumService.listPhotoAlbums` <= `PhotoAlbumServiceImpl.listPhotoAlbums`
  - `PhotoAlbumMapper.selectList`


## 9. 操作日志模块

- Controller: `OperationLogController`
- Service: 
  - `OperationLogService` <= `OperationLogServiceImpl`

操作日志模块用于记录系统的操作日志，包括操作记录的查看和删除功能。

### 9.1 查看操作日志 

- **功能描述** ：管理员可以查看操作日志，可以根据操作模块或描述进行筛选，并支持分页显示。
- **请求路径** ：`GET /admin/operation/logs`
- **请求参数** ： 
  - `ConditionVO`（请求体，包含操作日志的筛选条件，如操作模块、操作描述、分页信息等）
- **返回结果** ：`ResultVO<PageResultDTO<OperationLogDTO>>`（分页后的操作日志列表）
- **实现函数** ： 
  - `OperationLogController.listOperationLogs`
  - `OperationLogService.listOperationLogs` <= `OperationLogServiceImpl.listOperationLogs`

### 9.2 删除操作日志 

- **功能描述** ：管理员可以删除指定的操作日志，通过日志ID批量删除操作日志。
- **请求路径** ：`DELETE /admin/operation/logs`
- **请求参数** ： 
  - `operationLogIds`（请求体`List<Integer>`，包含操作日志ID的列表）
- **返回结果** ：`ResultVO`
- **实现函数** ： 
  - `OperationLogController.deleteOperationLogs`
  - `OperationLogService.removeByIds`


## 10. 菜单模块 

- Controller: `MenuController`
- Service: 
  - `MenuService` <= `MenuServiceImpl`
- Mapper: 
  - `MenuMapper`
  - `RoleMenuMapper`

菜单模块用于管理系统中的菜单，包括菜单的增、删、改、查功能，以及与角色和用户相关的菜单配置。

### 10.1 查看菜单列表 

- **功能描述** ：管理员可以查看系统菜单列表，可以根据菜单名称进行模糊搜索，返回的菜单列表支持树形结构展示，按照 `orderNum` 排序。
- **请求路径** ：`GET /admin/menus`
- **请求参数** ：`ConditionVO`（包含菜单名称关键词，用于模糊搜索）
- **返回结果** ：`ResultVO<List<MenuDTO>>`（菜单列表，按树形结构组织）
- **实现函数** ： 
  - `MenuController.listMenus`
  - `MenuService.listMenus` <= `MenuServiceImpl.listMenus`
  - `MenuMapper.selectList`
  - `MenuService.listCatalogs`
  - `MenuService.getMenuMap`

### 10.2 新增或修改菜单 

- **功能描述** ：管理员可以新增或修改菜单。
- **请求路径** ：`POST /admin/menus`
- **请求参数** ：`MenuVO`（菜单信息）
- **返回结果** ：`ResultVO`（操作结果）
- **实现函数** ： 
  - `MenuController.saveOrUpdateMenu`
  - `MenuService.saveOrUpdateMenu` <= `MenuServiceImpl.saveOrUpdateMenu`
  - `MenuService.saveOrUpdate`

### 10.3 修改菜单是否隐藏 

- **功能描述** ：管理员可以修改菜单的 `isHidden` 状态，决定菜单是否在前端显示。
- **请求路径** ：`PUT /admin/menus/isHidden`
- **请求参数** ：`IsHiddenVO`（菜单ID和隐藏状态）
- **返回结果** ：`ResultVO`（操作结果）
- **实现函数** ： 
  - `MenuController.updateMenuIsHidden`
  - `MenuService.updateMenuIsHidden` <= `MenuServiceImpl.updateMenuIsHidden`
  - `MenuMapper.updateById`

### 10.4 删除菜单 

- **功能描述** ：管理员可以删除指定的菜单。如果该菜单下存在角色关联或子菜单，删除时需要进行处理。
- **请求路径** ：`DELETE /admin/menus/{menuId}`
- **请求参数** ：`menuId`（Integer，菜单ID）
- **返回结果** ：`ResultVO`（操作结果）
- **实现函数** ： 
  - `MenuController.deleteMenu`
  - `MenuService.deleteMenu` <= `MenuServiceImpl.deleteMenu`
  - `RoleMenuMapper.selectCount`
  - `MenuMapper.selectList`
  - `MenuMapper.deleteBatchIds`

### 10.5 查看角色菜单选项 

- **功能描述** ：管理员查看角色对应的菜单选项，通常用于角色权限配置。
- **请求路径** ：`GET /admin/role/menus`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<LabelOptionDTO>>`（角色菜单选项）
- **实现函数** ： 
  - `MenuController.listMenuOptions`
  - `MenuService.listMenuOptions` <= `MenuServiceImpl.listMenuOptions`
  - `MenuMapper.selectList`
  - `MenuService.listCatalogs`
  - `MenuService.getMenuMap`

### 10.6 查看当前用户菜单 

- **功能描述** ：获取当前登录用户的菜单列表，返回的是用户可以访问的菜单。
- **请求路径** ：`GET /admin/user/menus`
- **请求参数** ：无
- **返回结果** ：`ResultVO<List<UserMenuDTO>>`（用户菜单列表）
- **实现函数** ： 
  - `MenuController.listUserMenus`
  - `MenuService.listUserMenus` <= `MenuServiceImpl.listUserMenus`
  - `MenuMapper.listMenusByUserInfoId`
  - `MenuService.listCatalogs`
  - `MenuService.getMenuMap`
  - `MenuService.convertUserMenuList`
