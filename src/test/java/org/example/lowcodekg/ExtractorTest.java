package org.example.lowcodekg;

import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.example.lowcodekg.model.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.extraction.page.PageExtractor;
import org.example.lowcodekg.model.schema.entity.page.Component;
import org.example.lowcodekg.model.schema.entity.page.ConfigItem;
import org.example.lowcodekg.model.schema.entity.page.PageTemplate;
import org.example.lowcodekg.service.FunctionalityGenService;
import org.example.lowcodekg.service.ClineService;
import org.example.lowcodekg.service.LLMGenerateService;
import org.example.lowcodekg.common.util.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.lowcodekg.common.util.PageParserUtil.getTemplateContent;

@SpringBootTest
public class ExtractorTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private ClineService clineService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private FunctionalityGenService functionalityGenService;
    @Autowired
    private ComponentRepo componentRepo;

    @Test
    public void TestFuncGen() {
        String str = """
                以下给出一个软件项目中多个请求-响应的执行工作流的描述信息，请你根据给出的功能与技术描述对其进行分类，形成一个树形结构的软件项目功能架构树。
                1. {"功能概况":"实现分页查询定时任务列表的功能。","执行逻辑":"接收页码和每页条数参数，调用服务层获取定时任务列表，并使用PageHelper进行分页处理，最后返回包含分页信息的结果。","技术特征":"使用了Spring MVC框架处理HTTP请求，PageHelper库进行分页处理，Result类封装响应结果。"}
                2. {"执行逻辑":"接收友链ID，调用服务层删除友链，返回删除成功的消息。","功能概括":"删除友链","技术特征":"Spring Boot框架，@DeleteMapping注解，Result类"}
                3. {"功能概况":"分页查询登录日志列表","执行逻辑":"接收操作时间范围、页码和每页个数作为参数，调用服务层方法获取登录日志列表，并按创建时间降序排序，返回分页结果。","技术特征":"Spring Boot, MyBatis, PageHelper"}
                4. {"功能概况":"删除指定ID的任务日志","执行逻辑":"接收任务日志的ID，调用服务层方法删除对应ID的日志，并返回删除成功的消息。","技术特征":"Spring Boot框架（通过@DeleteMapping注解），自定义Result类用于封装响应结果"}
                5. {"功能概况":"删除指定ID的访问日志","执行逻辑":"接收请求参数ID，调用服务层方法删除对应ID的访问日志，返回删除成功的消息。","技术特征":"Spring Boot框架，使用@RestController和@DeleteMapping注解处理HTTP DELETE请求，Result类用于封装响应结果。"}
                6. {"功能概况":"获取关于我页面的配置信息","执行逻辑":"用户访问/about接口，系统调用aboutService.getAboutSetting()方法获取配置信息，并返回给用户。","技术特征":"Spring Boot框架, Result类"}
                7. {"功能概况":"发布动态","执行逻辑":"接收动态实体，设置创建时间为当前时间，调用服务层保存动态，并返回成功信息。","技术特征":"Spring Boot, @PostMapping, Date, Result"}
                8. {"执行逻辑":"接收页码和每页条数参数，调用服务层获取友链列表，并使用PageHelper进行分页处理，最后返回分页结果。","功能概括":"获取友链列表并进行分页","技术特征":"Spring Boot, MyBatis, PageHelper"}
                9. {"功能概况":"更新博客的置顶状态","执行逻辑":"接收请求参数，调用服务层方法更新博客的置顶状态，返回操作成功的信息","技术特征":"Spring Boot框架，使用@PutMapping注解处理HTTP PUT请求，调用blogService.updateBlogTopById方法进行业务逻辑处理"}
                10. null
                11. {"功能概况":"更新博客文章","执行逻辑":"接收博客文章DTO，校验参数合法性，处理分类和标签，维护博客标签关联表，最后更新或保存博客文章。","技术特征":"Spring Boot, MyBatis, Java"}
                12. {"功能概况":"实现用户对动态的点赞功能，并限制每天只能点赞一次。","执行逻辑":"接收用户的点赞请求，调用服务层方法增加动态的点赞数，并返回点赞成功的消息。","技术特征":"使用了Spring Boot框架进行开发，包含@AccessLimit和@VisitLogger注解用于实现访问限制和日志记录。"}
                13. {"执行逻辑":"接收博客ID和推荐状态，调用服务层方法更新数据库中的推荐状态，返回操作成功的消息。","功能概括":"更新博客的推荐状态","技术特征":"Spring Boot框架（@OperationLogger、@PutMapping），自定义Result类用于封装响应结果"}
                14. {"执行逻辑":"接收任务ID作为参数，调用服务层方法删除该任务，并返回删除成功的消息。","功能概括":"删除指定ID的定时任务","技术特征":"Spring Boot框架（@OperationLogger、@DeleteMapping），自定义Result类"}
                15. {"功能概况":"提供分页查询访问日志的功能，支持按访客标识码和访问时间进行模糊查询。","执行逻辑":"接收请求参数，处理日期范围，调用服务层获取访问日志列表，并返回分页结果。","技术特征":"使用Spring MVC框架处理HTTP请求；使操作。"}
                16. {"功能概况":"获取分类列表和标签列表","执行逻辑":"调用categoryService的getCategoryList方法获取分类列表，调用tagService的getTagList方法获取标签列表，将两者放入Map中返回。","技术特征":"Spring Boot框架，Result类"}
                17. {"执行逻辑":"接收前端传入的Map对象，调用aboutService的updateAbout方法进行更新操作，最后返回一个表示成功的Result对象。","功能概括":"更新关于我页面的内容","技术特征":"Spring Boot框架（@OperationLogger、@PutMapping），自定义Result类"}
                18. {"功能概况":"获取公开博客详情，支持密码保护文章的访问。","执行逻辑":"根据传入的博客ID和JWT Token，首先检查博客是否为公开状态。如果是密码保护的文章，则验证JWT Token的有效性。如果Token有效且匹配，则返回博客详情并更新ng Boot框架进行开发，包含MyBatis作为ORM工具，Redis用于缓存浏览量，JWT库用于处理Token的生成和验证。"}
                19. {"执行逻辑":"接收日期范围、页码和每页条数参数，调用服务层方法获取定时任务日志列表，并按创建时间降序排序，返回分页结果。","功能概括":"分页查询定时任务日志列表","技术特征":"Spring Boot, MyBatis, PageHelper"}
                20. null
                21. {"功能概况":"修改或添加标签","执行逻辑":"接收标签实体，校验参数是否合法，检查标签是否已存在，然后调用相应的方法进行保存或更新操作，并返回结果。","技术特征":"Spring Boot, MyBatis, 自定义注解"}
                22. {"功能概况":"删除博客文章及其所有评论，并维护blog_tag表","执行逻辑":"接收文章ID，依次调用服务层方法删除博客、评论和关联的标签","技术特征":"Spring Boot, MyBatis"}
                23. {"功能概况":"实现添加或更新标签的功能。","执行逻辑":"接收标签实体，校验参数是否合法，检查标签是否已存在，然后调用相应的方法进行保存或更新操作，并返回结果。","技术特征":"使用了Spring Boot框架，包含@OperationLogger、@PostMapping注解；使用了自定义的Result类和StringUtils工具类；涉及数据库操作，通过tagService进行标签的保存和更新。"}
                24. {"功能概况":"更新友链","执行逻辑":"接收友链DTO，调用friendService的updateFriend方法更新友链，返回成功信息。","技术特征":"Spring Boot框架，@PutMapping注解，Result类"}
                25. {"执行逻辑":"接收请求参数id，调用blogService的getBlogById方法获取博客详情，返回包含博客详情的结果。","功能概括":"按id获取博客详情","技术特征":"Spring Boot框架，使用@GetMapping注解处理HTTP GET请求，Result类用于封装响应结果。"}
                26. {"执行逻辑":"通过调用blogService.getIdAndTitleList()方法获取博客列表，然后返回包含博客ID和标题的结果。","功能概括":"获取所有博客的ID和标题，供评论分类选择","技术特征":"Spring Boot框架, 自定义Result类"}
                27. {"执行逻辑":"接收友链DTO对象，调用friendService的saveFriend方法保存友链，返回成功信息","功能概括":"实现友链的添加功能","技术特征":"Spring Boot框架, @PostMapping注解, Result类"}
                28. null
                29. {"功能概况":"获取友链页面的数据，包括友链列表和友链信息。","执行逻辑":"调用friendService的getFriendVOList方法获取友链列表，调用getFriendInfo方法获取友链信息，并将两者封装到Map中返回。","技术特征":"使用了Spring MVC框ce服务层。"}
                30. {"功能概况":"按页面和博客ID分页查询评论列表","执行逻辑":"接收请求参数，调用服务层方法获取评论列表，并进行分页处理，最后返回结果。","技术特征":"Spring Boot, MyBatis, PageHelper"}
                31. {"功能概况":"更新友链页面的内容","执行逻辑":"接收包含内容的JSON对象，调用服务层方法更新友链页面内容，并返回成功信息","技术特征":"Spring Boot框架, @PutMapping注解, Result类"}
                32. {"功能概况":"修改友链页面的评论开放状态","执行逻辑":"接收前端传入的评论开放状态参数，调用服务层方法更新友链页面的评论开放状态，并返回操作成功的消息。","技术特征":"Spring Boot框架（通过@OperationLogger、@PutMapping等注解），自定义Result类用于封装响应结果"}
                33. {"功能概况":"更新动态的公开状态","执行逻辑":"接收动态ID和公开状态，调用服务层方法更新动态的公开状态，并返回操作成功的消息。","技术特征":"Spring Boot框架（@OperationLogger、@PutMapping），自定义Result类"}
                34. {"功能概况":"删除指定ID的评论及其所有子评论","执行逻辑":"接收评论ID，调用服务层方法删除评论及其子评论，返回删除成功的消息","技术特征":"Spring Boot框架，使用@DeleteMapping注解处理HTTP DELETE请求，Result类用于封装响应结果"}
                35. {"功能概况":"获取仪表盘数据，包括今日访问量、独立访客数、博客数量、评论数量以及各类别的博客和标签统计信息。","执行逻辑":"通过调用多个服务方法获取所需的数据，并将这些数据封装到一个Map中返回。","技术特征":"使用Spring MVC框架处理HTTP请求；依赖RedisService进行缓存操作；涉及自定义的Result类用于封装响应结果。"}
                36. {"功能概况":"删除指定ID的动态","执行逻辑":"接收动态ID作为参数，调用服务层方法删除对应动态，并返回删除成功的消息。","技术特征":"Spring Boot框架（@OperationLogger、@DeleteMapping），自定义Result类"}
                37. {"功能概况":"根据标签名称分页查询公开博客列表","执行逻辑":"接收标签名称和页码参数，调用服务层方法获取博客信息，并返回结果。","技术特征":"Spring Boot框架，使用@GetMapping注解处理HTTP请求，@RequestParam用于获取请求参数，PageResult用于分页数据封装，Result用于统一响应格式。"}
                38. {"功能概况":"获取博客文章列表，并按标题和分类ID进行模糊查询，返回分页结果及分类列表。","执行逻辑":"接收请求参数，调用服务层方法获取博客文章列表和分类列表，封装结果并返回。","技术特征":"Spring Boot, MyBatis, PageHelper"}
                39. {"功能概况":"根据分类名称分页查询公开博客列表","执行逻辑":"接收分类名称和页码参数，调用服务层方法获取博客信息，并返回结果。","技术特征":"Spring Boot框架, MyBatis或JPA等ORM工具"}
                40. {"功能概况":"立即执行指定ID的任务","执行逻辑":"接收任务ID，调用服务层方法runJobById执行任务，并返回成功信息","技术特征":"Spring Boot框架, @PostMapping注解, Result类"}
                41. {"功能概况":"获取博客分类列表","执行逻辑":"接收页码和每页个数参数，调用categoryService获取分类列表，并分页返回结果。","技术特征":"Spring Boot框架，PageHelper分页插件"}
                42. {"功能概况":"修改、删除和添加站点配置","执行逻辑":"接收包含更新后的站点配置列表和要删除的配置ID列表的请求，调用服务层方法进行处理，最后返回成功信息。","技术特征":"Spring Boot, 自定义注解@OperationLogger, Result类"}
                43. {"功能概况":"提供分页查询操作日志的功能，支持按操作时间范围和分页参数进行查询。","执行逻辑":"接收请求参数，解析日期范围，设置排序条件，调用服务层获取操作日志列表，并返回分页结果。","技术特征":"使用Spring MVC框架处理求；使用PageHelper库实现分页功能；使用MyBatis或JPA进行数据库操作。"}
                44. {"功能概况":"实现博客文章的保存和发布功能，包括校验参数、处理分类和标签、维护博客标签关联表等。","执行逻辑":"接收博客文章DTO，进行参数校验，处理分类和标签，更新或添加博客，并维护博客标签关联表。","技术特征":"使用Spring Boot框架，包含@OperationLogger注解；使用MyBatis进行数据库操作；涉及StringUtils工具类、Result结果封装类等。"}
                45. {"功能概况":"实现博主身份登录并签发Token","执行逻辑":"接收用户登录信息，验证用户名和密码，检查角色是否为管理员，生成JWT Token并返回给客户端","技术特征":"Spring Boot, JWT (Java Web Tokens), 自定义Result类"}
                46. {"功能概况":"删除指定ID的操作日志","执行逻辑":"接收操作日志的ID，调用服务层方法删除对应ID的操作日志，并返回删除成功的消息。","技术特征":"Spring Boot框架，使用@RestController和@DeleteMapping注解处理HTTP请求，Result类用于封装响应结果。"}
                47. {"功能概况":"提供关于我页面的信息","执行逻辑":"用户访问/about接口，系统调用aboutService.getAboutInfo()方法获取信息，并返回成功消息和数据。","技术特征":"Spring MVC框架, 自定义注解@VisitLogger, 自定义Result类"}
                48. {"功能概况":"实现对分类的添加和更新操作，校验分类名称是否为空或已存在。","执行逻辑":"接收分类实体，校验参数合法性，查询分类是否存在，根据类型（添加或更新）调用相应服务方法，并返回结果。","技术特征":"使用Spring Boot框架，包含@OperationLogger、@PostMapping注解；使用MyBatis进行数据库操作；依赖StringUtils工具类进行字符串校验。"}
                49. {"功能概况":"删除指定ID的登录日志","执行逻辑":"接收一个登录日志ID作为参数，调用服务层方法删除该ID对应的登录日志，并返回删除成功的消息。","技术特征":"Spring Boot框架（使用@RestController和@DeleteMapping注解），自定义Result类用于封装响应结果"}
                50. {"功能概况":"更新用户动态","执行逻辑":"接收动态实体，设置创建时间为当前时间（如果未设置），调用服务层更新动态，并返回成功消息。","技术特征":"Spring Boot框架，@PutMapping注解，Moment实体类，momentService服务层，Result工具类"}
                51. {"功能概况":"修改或添加分类名称","执行逻辑":"接收分类实体，校验分类名称是否为空和是否已存在，然后调用相应的方法进行保存或更新操作，并返回结果。","技术特征":"Spring Boot框架，使用@PutMapping注解处理HTTP PUT请求，涉及自定义的Result类和CategoryService接口及其实现。"}
                52. {"功能概况":"获取博客标签列表","执行逻辑":"接收页码和每页个数参数，调用tagService获取标签列表，并分页返回结果。","技术特征":"Spring Boot框架，PageHelper分页插件"}
                53. {"功能概况":"删除指定ID的标签，如果该标签与任何博客关联，则不允许删除并返回错误信息。","执行逻辑":"接收标签ID作为参数，调用blogService检查是否存在关联的博客。如果没有关联的博客，则调用tagService删除标签，并返回删除。涉及Result类用于封装响应结果，blogService和tagService是业务逻辑服务。"}
                54. {"功能概况":"按置顶和创建时间排序，分页查询已发布的博客简要信息列表。","执行逻辑":"接收请求参数页码，调用服务层方法获取已发布博客的简要信息，并返回结果。","技术特征":"使用Spring MVC框架处理HTTP请求；使用自定义注解@VisitLogger记录访问行为；返回Result对象封装响应数据。"}
                55. {"功能概况":"更新评论接收邮件提醒状态","执行逻辑":"接收评论ID和是否接收提醒的参数，调用服务层方法更新评论的提醒状态，并返回操作成功的消息。","技术特征":"Spring Boot框架, 自定义注解@OperationLogger, 自定义Result类"}
                56. {"功能概况":"增加友链的浏览次数","执行逻辑":"接收友链昵称作为参数，调用服务层方法更新友链的浏览次数，并返回成功信息。","技术特征":"Spring Boot框架, @PostMapping注解, Result类"}
                57. {"功能概况":"分页查询异常日志列表","执行逻辑":"接收操作时间范围、页码和每页个数作为参数，调用服务层方法获取异常日志列表，并按创建时间降序排序，返回分页结果。","技术特征":"Spring Boot, MyBatis, PageHelper"}
                58. {"功能概况":"更新博客的可见性状态","执行逻辑":"接收博客ID和博客可见性DTO，调用服务层方法更新博客可见性，并返回操作成功的信息。","技术特征":"Spring Boot框架, @OperationLogger注解, Result类"}
                59. {"功能概况":"修改评论","执行逻辑":"接收前端传来的评论实体，校验参数是否为空，如果不为空则调用commentService的updateComment方法更新评论，并返回成功信息；如果参数为空，则返回错误信息。","技术特征":"Spring Boot框架（@OperationLogger、@PutMapping），自定义Result类用于封装响应结果，StringUtils工具类用于字符串校验"}
                60. {"功能概况":"创建一个新的定时任务","执行逻辑":"接收一个定时任务对象，设置其状态和创建时间，进行校验后保存到数据库，并返回成功信息。","技术特征":"Spring Boot, ValidatorUtils (自定义校验工具), Result (响应结果封装)"}
                61. {"功能概况":"实现分页查询动态列表的功能。","执行逻辑":"接收页码和每页条数参数，调用服务层获取动态列表数据，并进行分页处理，最后返回分页结果。","技术特征":"使用了Spring Boot框架的@RestController注解来定义控制器；使用r库进行分页查询；使用了MyBatis或JPA等ORM框架来操作数据库。"}
                62. {"功能概况":"获取所有站点配置信息","执行逻辑":"通过调用siteSettingService的getList方法获取站点配置信息，并将结果封装在Result对象中返回。","技术特征":"Spring Boot框架，使用@GetMapping注解处理HTTP GET请求，Result类用于封装响应数据。"}
                63. {"功能概况":"更新友链的公开状态","执行逻辑":"接收友链ID和公开状态，调用服务层方法更新友链的公开状态，并返回操作成功的消息。","技术特征":"Spring Boot框架, 自定义注解@OperationLogger, 自定义Result类"}
                64. {"功能概况":"实现分页查询动态列表的功能，支持博主身份验证。","执行逻辑":"接收页码和博主访问Token作为参数，判断Token是否有效并解析出博主身份。调用服务层获取动态列表，并封装成分页结果返回给客户端。","技术特征":"使用Sng Boot框架进行开发；JWT（JSON Web Token）用于身份验证；MyBatis作为持久层框架；PageHelper插件实现分页功能；自定义Result类封装响应数据。"}
                65. {"功能概况":"删除访客并清除Redis缓存","执行逻辑":"接收访客id和uuid，调用visitorService的deleteVisitor方法删除访客，并通过Result返回删除成功的消息","技术特征":"Spring Boot框架, Redis"}
                66. {"功能概况":"修改定时任务的状态","执行逻辑":"接收一个包含定时任务信息的请求，设置任务状态为false，并进行校验，然后调用服务层更新任务。","技术特征":"Spring Boot, ValidatorUtils, Result"}
                67. {"功能概况":"校验博客密码并返回JWT令牌","执行逻辑":"接收博客ID和密码，调用服务获取存储的密码，比较密码是否正确。如果正确，则生成一个有效期为一个月的JWT令牌并返回；否则返回错误信息。","技术特征":"Spring Boot框架, JWT库"}
                68. {"功能概况":"获取友链页面信息","执行逻辑":"通过调用friendService的getFriendInfo方法，返回友链页面的信息。","技术特征":"使用了Spring MVC框架，包含@GetMapping注解；使用了自定义的Result类来封装返回结果。"}
                69. {"功能概况":"删除指定ID的分类，并检查该分类下是否有博客关联，如果有则不允许删除。","执行逻辑":"接收分类ID作为参数，调用blogService检查该分类下是否有博客关联。如果没有关联，则调用categoryService删除该分类并返回成功信三方库包括Result类用于封装响应结果，blogService和categoryService用于业务逻辑处理。"}
                70. {"功能概况":"根据关键字搜索公开且无密码保护的博客文章","执行逻辑":"接收用户输入的关键字，校验其合法性，然后调用服务层方法获取符合条件的文章列表，并返回结果。","技术特征":"Spring MVC, MyBatis, Apache Commons Lang"}
                71. {"功能概况":"删除指定ID的异常日志","执行逻辑":"接收请求参数id，调用exceptionLogService的deleteExceptionLogById方法删除指定ID的异常日志，返回删除成功的消息。","技术特征":"Spring Boot框架（使用@RestController和@DeleteMapping注解），自定义Result类用于封装响应结果"}
                72. {"功能概况":"实现用户账号密码的修改功能。","执行逻辑":"接收前端传来的用户信息和JWT令牌，调用userService的changeAccount方法进行账号密码修改，根据返回结果返回相应的成功或失败消息。","技术特征":"使用Spring Boot框架，包含@RestController注解；使用JWT进行身份验证；自定义Result类用于封装响应结果。"}
                73. {"功能概况":"更新任务状态，支持暂停和恢复","执行逻辑":"接收任务ID和状态参数，调用服务层方法更新任务状态，并返回成功信息","技术特征":"Spring Boot框架，使用@PutMapping注解处理HTTP PUT请求，Result类封装响应结果"}
                74. {"功能概况":"更新评论的公开状态","执行逻辑":"接收评论ID和公开状态，调用服务层方法更新评论的公开状态，并返回操作成功的消息。","技术特征":"Spring Boot框架（@OperationLogger、@PutMapping），自定义Result类用于封装响应结果"}
                75. {"功能概况":"实现分页查询访客列表的功能，支持按最后访问时间筛选，并返回指定页码和每页个数的访客数据。","执行逻辑":"接收请求参数（日期范围、页码、每页个数），处理日期范围，设置排序条件，调用服务层获取访客列表，分页处理后返回结果。","技术特征":"使用Spring Boot框架进行开发，包含PageHelper库用于分页查询，Result类用于封装响应数据。"}
                76. {"功能概况":"按年月分组归档公开博客，并统计公开博客总数。","执行逻辑":"调用blogService的getArchiveBlogAndCountByIsPublished方法获取数据，然后返回包含年月分组和博客数量的结果。","技术特征":"使用了Spring MVC框架进行W用了自定义的日志记录器VisitLogger。"}
                77. {"功能概况":"查询网页标题后缀","执行逻辑":"用户访问/webTitleSuffix接口，系统调用siteSettingService的getWebTitleSuffix方法获取网页标题后缀，并返回结果。","技术特征":"Spring Boot框架, 自定义Result类"}
                78. {"功能概况":"根据动态ID查询动态内容","执行逻辑":"接收请求参数ID，调用服务层方法获取动态数据，返回包含动态信息的结果。","技术特征":"Spring Boot框架, Result类"}
                79. {"功能概况":"获取站点配置信息、最新推荐博客、分类列表、标签云和随机博客","执行逻辑":"调用多个服务方法获取所需数据，并将这些数据封装到一个Map中，最后返回包含所有数据的Result对象","技术特征":"Spring Boot框架, 自定义R}
                请严格按照以下json格式返回结果
                [
                    {
                        "module": "",
                        "description": "",
                        "workflowList": [] // 列表包含该模块下工作流的序号
                    }
                ]
                """;
        String result = llmGenerateService.generateAnswer(str);
        System.out.println(result);
//        if(result.startsWith("```json")) {
//            result = result.substring(8, result.length() - 3);
//        }
//        System.out.println(result);
//        JSONObject jsonObject = JSONObject.parseObject(result);
//        for(String key : jsonObject.keySet()) {
//            System.out.println(key + ": " + jsonObject.get(key));
//        }
    }

    @Test
    public void test() {
        String prompt = """
                给定下面的代码内容，你的任务是对其进行解析返回一个json对象。注意，如果key对应的value包含了表达式或函数调用，将其转为字符串格式
                比如：对于
                "headers": {
                    "Authorization": "Bearer " + sessionStorage.getItem('token')
               }，应该表示为：
                "headers": {
                    "Authorization": "'Bearer ' + sessionStorage.getItem('token')"
                  }
                
                下面是给出的代码片段，请返回json结果:
                {content}
                
                """;
        String content = """
                 {       radioValue: 1,      cycle01: 1,      cycle02: 2,      average01: 0,      average02: 1,      checkboxList: [],      checkNum: this.$options.propsData.check    }
                
                """;
        prompt = prompt.replace("{content}", content);
        JSONObject jsonObject = new JSONObject();
        try {
            prompt = prompt.replace("{content}", content);
            String answer = llmGenerateService.generateAnswer(prompt);
            if(answer.contains("```json")) {
                answer = answer.substring(answer.indexOf("```json") + 7, answer.lastIndexOf("```"));
            }
            System.out.println(answer);
            jsonObject = JSONObject.parseObject(answer);
            jsonObject.forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVueParser() {
        String path = "";
        path = "/Users/chang/Documents/projects/data_projects/NBlog/blog-cms/src/views/page/FriendList.vue";
        File vueFile = new File(path);
        System.out.println(vueFile.getName());

        PageTemplate pageTemplate = new PageTemplate();
        pageTemplate.setName(vueFile.getName());
        String fileContent = FileUtil.readFile(vueFile.getAbsolutePath());

        PageExtractor pageExtractor = new PageExtractor();

        // parse template
        String templateContent = getTemplateContent(fileContent);
        if(!Objects.isNull(templateContent)) {
            Document document = Jsoup.parse(templateContent);
            Element divElement = document.selectFirst("Template");
            divElement.children().forEach(element -> {
                Component component = pageExtractor.parseTemplate(element, null);
                pageTemplate.getComponentList().add(component);
            });
        }
        for(Component component: pageTemplate.getComponentList()) {
            for(ConfigItem configItem: component.getConfigItemList()) {
                System.out.println("config item: " + configItem.getCode() + " " + configItem.getValue());
            }
        }

        // parse script
//        String content = pageExtractor.getScriptContent(fileContent);
//        if(content.length() != 0) {
//            Script script = new Script();
//            script.setContent(content);
//
//            // parse import components
//            JSONObject importsList = pageExtractor.parseImportsComponent(content);
//            script.setImportsComponentList(importsList.toString());
//
//            // parse data
////            JSONObject data = pageExtractor.parseScriptData(content);
////            script.setDataList(data);
//
//            // parse methods
//            List<Script.ScriptMethod> methodList = pageExtractor.parseScriptMethod(content);
//            script.setMethodList(methodList);
//            pageTemplate.setScript(script);
//        }
    }

    @Test
    public void textFunctionality() {
        String codeContent = """
                @PostMapping("/account")
                	public Result account(@RequestBody User user, @RequestHeader(value = "Authorization", defaultValue = "") String jwt) {
                		boolean res = userService.changeAccount(user, jwt);
                		return res ? Result.ok("修改成功") : Result.error("修改失败");
                	}
                
                @Override
                	public boolean changeAccount(User user, String jwt) {
                		String username = JwtUtils.getTokenBody(jwt).getSubject();
                		user.setPassword(HashUtils.getBC(user.getPassword()));
                		if (userMapper.updateUserByUsername(username, user) != 1) {
                			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                			return false;
                		}
                		return true;
                	}
                
                <!--按username修改-->
                    <update id="updateUserByUsername">
                        update user set username=#{user.username}, password=#{user.password}, update_time=now() where username=#{username}
                    </update>
                
                public static Claims getTokenBody(String token) {
                		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token.replace("Bearer", "")).getBody();
                		return claims;
                	}
                
                public static String getBC(CharSequence rawPassword) {
                		return bCryptPasswordEncoder.encode(rawPassword);
                	}
                
                """;
        String prompt = """
                    You are an expert in programming with a thorough understanding of software projects.
                    The content below provides the method calls and data objects involved in implementing a certain function request within a software project.
                    Based on the code provided, please summarize the implemented function and the technological frameworks, third-party libraries, etc., used during the implementation.
                    Your results should address three aspects: "功能概括," "执行逻辑," and "技术特征." Specifically:
                    * **功能概括**: Provide a concise description of the implemented function without involving technical details, keep it as short as possible.
                    * **执行逻辑**: Describe the overall process of code execution, minimizing technical details.
                    * **技术特征**: Mention any technological frameworks, third-party libraries, tools, etc., involved during the code execution.
                    
                    The code content you need to explain is as follows:
                    {codeContent}
                    
                    Please ensure the output is concise and not too lengthy, also in Chinese, while strictly following the JSON format below without including any additional content:
                    ```json
                    {
                        "功能概括": "",
                        "执行逻辑": "",
                        "技术特征": ""
                    }
                    ```
                    """;
        prompt = prompt.replace("{codeContent}", codeContent);
        String result = llmGenerateService.generateAnswer(prompt);
        System.out.println(result);
        Pattern p = Pattern.compile("```json\\s*(\\{[.\\d\\w\\s\\n\\D]*\\})\\s*```");
        Matcher m = p.matcher(result);
        if (m.find()) {
            result = m.group(1);
            System.out.println(result);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        System.out.println(jsonObject.getString("功能概括"));
    }
}
