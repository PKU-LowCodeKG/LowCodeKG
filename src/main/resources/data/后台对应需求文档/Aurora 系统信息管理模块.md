## Aurora 系统信息管理模块

### 上报访客信息

```java
//AuroraInfoServiceImpl.java
public void report() {
    String ipAddress = IpUtil.getIpAddress(request);
    UserAgent userAgent = IpUtil.getUserAgent(request);
    Browser browser = userAgent.getBrowser();
    OperatingSystem operatingSystem = userAgent.getOperatingSystem();
    String uuid = ipAddress + browser.getName() + operatingSystem.getName();
    String md5 = DigestUtils.md5DigestAsHex(uuid.getBytes());
    if (!redisService.sIsMember(UNIQUE_VISITOR, md5)) {
        String ipSource = IpUtil.getIpSource(ipAddress);
        if (StringUtils.isNotBlank(ipSource)) {
            String ipProvince = IpUtil.getIpProvince(ipSource);
            redisService.hIncr(VISITOR_AREA, ipProvince, 1L);
        } else {
            redisService.hIncr(VISITOR_AREA, UNKNOWN, 1L);
        }
        redisService.incr(BLOG_VIEWS_COUNT, 1);
        redisService.sAdd(UNIQUE_VISITOR, md5);
    }
}
//AuroraInfoController.java
public ResultVO<?> report() {
    auroraInfoService.report();
    return ResultVO.ok();
}
```

### 获取系统信息

```java
//AuroraInfoServiceImpl.java
public AuroraHomeInfoDTO getAuroraHomeInfo() {
    CompletableFuture<Integer> asyncArticleCount = CompletableFuture.supplyAsync(() -> articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getIsDelete, FALSE)));
    CompletableFuture<Integer> asyncCategoryCount = CompletableFuture.supplyAsync(() -> categoryMapper.selectCount(null));
    CompletableFuture<Integer> asyncTagCount = CompletableFuture.supplyAsync(() -> tagMapper.selectCount(null));
    CompletableFuture<Integer> asyncTalkCount = CompletableFuture.supplyAsync(() -> talkMapper.selectCount(null));
    CompletableFuture<WebsiteConfigDTO> asyncWebsiteConfig = CompletableFuture.supplyAsync(this::getWebsiteConfig);
    CompletableFuture<Integer> asyncViewCount = CompletableFuture.supplyAsync(() -> {
        Object count = redisService.get(BLOG_VIEWS_COUNT);
        return Integer.parseInt(Optional.ofNullable(count).orElse(0).toString());
    });
    return AuroraHomeInfoDTO.builder()
        .articleCount(asyncArticleCount.get())
        .categoryCount(asyncCategoryCount.get())
        .tagCount(asyncTagCount.get())
        .talkCount(asyncTalkCount.get())
        .websiteConfigDTO(asyncWebsiteConfig.get())
        .viewCount(asyncViewCount.get()).build();
}

//AuroraInfoController.java
public ResultVO<AuroraHomeInfoDTO> getBlogHomeInfo() {
    return ResultVO.ok(auroraInfoService.getAuroraHomeInfo());
}
```

### 获取系统后台信息

```java
//AuroraInfoServiceImpl.java
public AuroraAdminInfoDTO getAuroraAdminInfo() {
    Object count = redisService.get(BLOG_VIEWS_COUNT);
    Integer viewsCount = Integer.parseInt(Optional.ofNullable(count).orElse(0).toString());
    Integer messageCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getType, 2));
    Integer userCount = userInfoMapper.selectCount(null);
    Integer articleCount = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                                                     .eq(Article::getIsDelete, FALSE));
    List<UniqueViewDTO> uniqueViews = uniqueViewService.listUniqueViews();
    List<ArticleStatisticsDTO> articleStatisticsDTOs = articleMapper.listArticleStatistics();
    List<CategoryDTO> categoryDTOs = categoryMapper.listCategories();
    List<TagDTO> tagDTOs = BeanCopyUtil.copyList(tagMapper.selectList(null), TagDTO.class);
    Map<Object, Double> articleMap = redisService.zReverseRangeWithScore(ARTICLE_VIEWS_COUNT, 0, 4);
    AuroraAdminInfoDTO auroraAdminInfoDTO = AuroraAdminInfoDTO.builder()
        .articleStatisticsDTOs(articleStatisticsDTOs)
        .tagDTOs(tagDTOs)
        .viewsCount(viewsCount)
        .messageCount(messageCount)
        .userCount(userCount)
        .articleCount(articleCount)
        .categoryDTOs(categoryDTOs)
        .uniqueViewDTOs(uniqueViews)
        .build();
    if (CollectionUtils.isNotEmpty(articleMap)) {
        List<ArticleRankDTO> articleRankDTOList = listArticleRank(articleMap);
        auroraAdminInfoDTO.setArticleRankDTOs(articleRankDTOList);
    }
    return auroraAdminInfoDTO;
}
//AuroraInfoController.java
public ResultVO<AuroraAdminInfoDTO> getBlogBackInfo() {
    return ResultVO.ok(auroraInfoService.getAuroraAdminInfo());
}
```

### 更新网站配置

```java
//AuroraInfoServiceImpl.java
public void updateWebsiteConfig(WebsiteConfigVO websiteConfigVO) {
    WebsiteConfig websiteConfig = WebsiteConfig.builder()
        .id(DEFAULT_CONFIG_ID)
        .config(JSON.toJSONString(websiteConfigVO))
        .build();
    websiteConfigMapper.updateById(websiteConfig);
    redisService.del(WEBSITE_CONFIG);
}
//AuroraInfoController.java
public ResultVO<?> updateWebsiteConfig(@Valid @RequestBody WebsiteConfigVO websiteConfigVO) {
    auroraInfoService.updateWebsiteConfig(websiteConfigVO);
    return ResultVO.ok();
}
```

### 获取网站配置

```java
//AuroraInfoServiceImpl.java
public WebsiteConfigDTO getWebsiteConfig() {
    WebsiteConfigDTO websiteConfigDTO;
    Object websiteConfig = redisService.get(WEBSITE_CONFIG);
    if (Objects.nonNull(websiteConfig)) {
        websiteConfigDTO = JSON.parseObject(websiteConfig.toString(), WebsiteConfigDTO.class);
    } else {
        String config = websiteConfigMapper.selectById(DEFAULT_CONFIG_ID).getConfig();
        websiteConfigDTO = JSON.parseObject(config, WebsiteConfigDTO.class);
        redisService.set(WEBSITE_CONFIG, config);
    }
    return websiteConfigDTO;
}

//AuroraInfoController.java
public ResultVO<WebsiteConfigDTO> getWebsiteConfig() {
    return ResultVO.ok(auroraInfoService.getWebsiteConfig());
}
```

### 查看关于我的信息

```java
//AuroraInfoServiceImpl.java
public AboutDTO getAbout() {
    AboutDTO aboutDTO;
    Object about = redisService.get(ABOUT);
    if (Objects.nonNull(about)) {
        aboutDTO = JSON.parseObject(about.toString(), AboutDTO.class);
    } else {
        String content = aboutMapper.selectById(DEFAULT_ABOUT_ID).getContent();
        aboutDTO = JSON.parseObject(content, AboutDTO.class);
        redisService.set(ABOUT, content);
    }
    return aboutDTO;
}


//AuroraInfoController.java
public ResultVO<AboutDTO> getAbout() {
    return ResultVO.ok(auroraInfoService.getAbout());
}
```

### 修改关于我的信息

```java
//AuroraInfoServiceImpl.java
public void updateAbout(AboutVO aboutVO) {
    About about = About.builder()
        .id(DEFAULT_ABOUT_ID)
        .content(JSON.toJSONString(aboutVO))
        .build();
    aboutMapper.updateById(about);
    redisService.del(ABOUT);
}
//AuroraInfoController.java
public ResultVO<AboutDTO> getAbout() {
    return ResultVO.ok(auroraInfoService.getAbout());
}
```

### 上传博客配置图片

```java
//UploadStrategyContext.java
public class UploadStrategyContext {

    @Value("${upload.mode}")
    private String uploadMode;

    @Autowired
    private Map<String, UploadStrategy> uploadStrategyMap;

    public String executeUploadStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(getStrategy(uploadMode)).uploadFile(file, path);
    }

    public String executeUploadStrategy(String fileName, InputStream inputStream, String path) {
        return uploadStrategyMap.get(getStrategy(uploadMode)).uploadFile(fileName, inputStream, path);
    }

}
//AuroraInfoController.java
public ResultVO<String> savePhotoAlbumCover(MultipartFile file) {
    return ResultVO.ok(uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.CONFIG.getPath()));
}
```

