package org.example.lowcodekg.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.query.service.util.LLMService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class PromptTest {
    @Autowired
    private LLMGenerateService llmService;
    @Autowired
    private LLMService llm;

    @Test
    void test() {
        String prompt = """
                You are an expert in Software Engineering with extensive experience in analyzing functional implementations in software projects.
                
              Your task is to break down the given requirement into functional implementation-oriented subtasks based on the provided code context. Focus specifically on core functional components rather than technical details or non-functional aspects.

              **Key Focus Areas:**
                  1.Entity fields/data structure changes (e.g.,adding/modifying database fields)
                  2.Service layer method implementation (e.g.,creating/updating domain service methods)
                  3.Interface/API definition (e.g.,adding REST endpoints)
                  4.Business logic flow (e.g.,state change rules)
                  5.Domain object relationship adjustments

              **Negative Examples (to avoid):**
              × Cache handling × Test case writing × Performance optimization × Deployment configuration × Monitoring logs

              **Example Breakdown for "Pin Blog Post":**
                  1.Add an "is pinned"boolean field to the blog entity
                  2.Create a blog update service method to handle pinning status
                  3.Implement persistence of pinning status to the database
                  4.Add an interface for querying the pinning status of a post
                  5.Write business rules for validating pinning status

              **Provided Code:**
              [{"name":"updateTop_1690","description":"该代码用于更新博客的置顶状态，接收博客ID和是否置顶的布尔值作为参数，通过调用服务层方法更新数据，并返回成功结果。","content":"/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\nvoid updateBlogTopById(Long blogId, Boolean top);\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\n"},{"name":"updateRecommend_1769","description":"该代码片段用于更新博客的推荐状态。它通过HTTP PUT请求接收博客ID和是否推荐的布尔值参数，调用服务层方法进行更新，并返回操作成功的响应结果。","content":"/**\\r\\n\\t * 更新博客推荐状态\\r\\n\\t *\\r\\n\\t * @param id        博客id\\r\\n\\t * @param recommend 是否推荐\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客推荐状态\\")\\r\\n\\t@PutMapping(\\"/blog/recommend\\")\\r\\n\\tpublic Result updateRecommend(@RequestParam Long id, @RequestParam Boolean recommend) {\\r\\n\\t\\tblogService.updateBlogRecommendById(id, recommend);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 更新博客推荐状态\\r\\n\\t *\\r\\n\\t * @param id        博客id\\r\\n\\t * @param recommend 是否推荐\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客推荐状态\\")\\r\\n\\t@PutMapping(\\"/blog/recommend\\")\\r\\n\\tpublic Result updateRecommend(@RequestParam Long id, @RequestParam Boolean recommend) {\\r\\n\\t\\tblogService.updateBlogRecommendById(id, recommend);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\nvoid updateBlogRecommendById(Long blogId, Boolean recommend);\\n"},{"name":"delete_1831","description":"删除指定博客文章及其关联的所有评论和标签","content":"/**\\r\\n\\t * 删除博客文章、删除博客文章下的所有评论、同时维护 blog_tag 表\\r\\n\\t *\\r\\n\\t * @param id 文章id\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"删除博客\\")\\r\\n\\t@DeleteMapping(\\"/blog\\")\\r\\n\\tpublic Result delete(@RequestParam Long id) {\\r\\n\\t\\tblogService.deleteBlogTagByBlogId(id);\\r\\n\\t\\tblogService.deleteBlogById(id);\\r\\n\\t\\tcommentService.deleteCommentsByBlogId(id);\\r\\n\\t\\treturn Result.ok(\\"删除成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 删除博客文章、删除博客文章下的所有评论、同时维护 blog_tag 表\\r\\n\\t *\\r\\n\\t * @param id 文章id\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"删除博客\\")\\r\\n\\t@DeleteMapping(\\"/blog\\")\\r\\n\\tpublic Result delete(@RequestParam Long id) {\\r\\n\\t\\tblogService.deleteBlogTagByBlogId(id);\\r\\n\\t\\tblogService.deleteBlogById(id);\\r\\n\\t\\tcommentService.deleteCommentsByBlogId(id);\\r\\n\\t\\treturn Result.ok(\\"删除成功\\");\\r\\n\\t}\\nvoid deleteCommentsByBlogId(Long blogId);\\nvoid deleteBlogTagByBlogId(Long blogId);\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\nvoid deleteBlogById(Long id);\\n"},{"name":"Blog_1444","description":"该Java类表示一个博客文章实体，包含文章的基本信息（如标题、内容、描述等）、状态开关（如是否公开、推荐、赞赏等）、时间戳、统计数据（如浏览次数、字数、阅读时长）以及与用户、分类和标签的关联。","content":"/**\\r\\n * @Description: 博客文章\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-26\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Blog {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String title;//文章标题\\r\\n\\tprivate String firstPicture;//文章首图，用于随机文章展示\\r\\n\\tprivate String content;//文章正文\\r\\n\\tprivate String description;//描述\\r\\n\\tprivate Boolean published;//公开或私密\\r\\n\\tprivate Boolean recommend;//推荐开关\\r\\n\\tprivate Boolean appreciation;//赞赏开关\\r\\n\\tprivate Boolean commentEnabled;//评论开关\\r\\n\\tprivate Boolean top;//是否置顶\\r\\n\\tprivate Date createTime;//创建时间\\r\\n\\tprivate Date updateTime;//更新时间\\r\\n\\tprivate Integer views;//浏览次数\\r\\n\\tprivate Integer words;//文章字数\\r\\n\\tprivate Integer readTime;//阅读时长(分钟)\\r\\n\\tprivate String password;//密码保护\\r\\n\\r\\n\\tprivate User user;//文章作者(因为是个人博客，也可以不加作者字段，暂且加上)\\r\\n\\tprivate Category category;//文章分类\\r\\n\\tprivate List<Tag> tags = new ArrayList<>();//文章标签\\r\\n}"},{"name":"Comment_1418","description":"该Java类Comment表示博客评论功能模块，用于存储和管理评论相关信息，包括评论内容、评论者信息、评论时间、所属文章以及回复关系等。","content":"/**\\r\\n * @Description: 博客评论\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-27\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Comment {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String nickname;//昵称\\r\\n\\tprivate String email;//邮箱\\r\\n\\tprivate String content;//评论内容\\r\\n\\tprivate String avatar;//头像(图片路径)\\r\\n\\tprivate Date createTime;//评论时间\\r\\n\\tprivate String website;//个人网站\\r\\n\\tprivate String ip;//评论者ip地址\\r\\n\\tprivate Boolean published;//公开或回收站\\r\\n\\tprivate Boolean adminComment;//博主回复\\r\\n\\tprivate Integer page;//0普通文章，1关于我页面\\r\\n\\tprivate Boolean notice;//接收邮件提醒\\r\\n\\tprivate Long parentCommentId;//父评论id\\r\\n\\tprivate String qq;//如果评论昵称为QQ号，则将昵称和头像置为QQ昵称和QQ头像，并将此字段置为QQ号备份\\r\\n\\r\\n\\tprivate BlogIdAndTitle blog;//所属的文章\\r\\n\\tprivate List<Comment> replyComments = new ArrayList<>();//回复该评论的评论\\r\\n}"},{"name":"Category_1542","description":"该代码定义了一个名为Category的Java类，用于表示博客分类。该类包含了分类的唯一标识(id)、分类名称(name)和一个存储该分类下所有博客文章(blogs)的列表。","content":"/**\\r\\n * @Description: 博客分类\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-26\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Category {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String name;//分类名称\\r\\n\\tprivate List<Blog> blogs = new ArrayList<>();//该分Task split prompt:
              You are an expert in Software Engineering with extensive experience in analyzing functional implementations in software projects.

              Your task is to break down the given requirement into functional implementation-oriented subtasks based on the provided code context. Focus specifically on core functional components rather than technical details or non-functional aspects.

              **Key Focus Areas:**
                  1.Entity fields/data structure changes (e.g.,adding/modifying database fields)
                  2.Service layer method implementation (e.g.,creating/updating domain service methods)
                  3.Interface/API definition (e.g.,adding REST endpoints)
                  4.Business logic flow (e.g.,state change rules)
                  5.Domain object relationship adjustments

              **Negative Examples (to avoid):**
              × Cache handling × Test case writing × Performance optimization × Deployment configuration × Monitoring logs

              **Example Breakdown for "Pin Blog Post":**
                  1.Add an "is pinned"boolean field to the blog entity
                  2.Create a blog update service method to handle pinning status
                  3.Implement persistence of pinning status to the database
                  4.Add an interface for querying the pinning status of a post
                  5.Write business rules for validating pinning status

              **Provided Code:**
              [{"name":"updateTop_1690","description":"该代码用于更新博客的置顶状态，接收博客ID和是否置顶的布尔值作为参数，通过调用服务层方法更新数据，并返回成功结果。","content":"/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\nvoid updateBlogTopById(Long blogId, Boolean top);\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\n"},{"name":"updateRecommend_1769","description":"该代码片段用于更新博客的推荐状态。它通过HTTP PUT请求接收博客ID和是否推荐的布尔值参数，调用服务层方法进行更新，并返回操作成功的响应结果。","content":"/**\\r\\n\\t * 更新博客推荐状态\\r\\n\\t *\\r\\n\\t * @param id        博客id\\r\\n\\t * @param recommend 是否推荐\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客推荐状态\\")\\r\\n\\t@PutMapping(\\"/blog/recommend\\")\\r\\n\\tpublic Result updateRecommend(@RequestParam Long id, @RequestParam Boolean recommend) {\\r\\n\\t\\tblogService.updateBlogRecommendById(id, recommend);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 更新博客推荐状态\\r\\n\\t *\\r\\n\\t * @param id        博客id\\r\\n\\t * @param recommend 是否推荐\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客推荐状态\\")\\r\\n\\t@PutMapping(\\"/blog/recommend\\")\\r\\n\\tpublic Result updateRecommend(@RequestParam Long id, @RequestParam Boolean recommend) {\\r\\n\\t\\tblogService.updateBlogRecommendById(id, recommend);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\nvoid updateBlogRecommendById(Long blogId, Boolean recommend);\\n"},{"name":"delete_1831","description":"删除指定博客文章及其关联的所有评论和标签","content":"/**\\r\\n\\t * 删除博客文章、删除博客文章下的所有评论、同时维护 blog_tag 表\\r\\n\\t *\\r\\n\\t * @param id 文章id\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"删除博客\\")\\r\\n\\t@DeleteMapping(\\"/blog\\")\\r\\n\\tpublic Result delete(@RequestParam Long id) {\\r\\n\\t\\tblogService.deleteBlogTagByBlogId(id);\\r\\n\\t\\tblogService.deleteBlogById(id);\\r\\n\\t\\tcommentService.deleteCommentsByBlogId(id);\\r\\n\\t\\treturn Result.ok(\\"删除成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 删除博客文章、删除博客文章下的所有评论、同时维护 blog_tag 表\\r\\n\\t *\\r\\n\\t * @param id 文章id\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"删除博客\\")\\r\\n\\t@DeleteMapping(\\"/blog\\")\\r\\n\\tpublic Result delete(@RequestParam Long id) {\\r\\n\\t\\tblogService.deleteBlogTagByBlogId(id);\\r\\n\\t\\tblogService.deleteBlogById(id);\\r\\n\\t\\tcommentService.deleteCommentsByBlogId(id);\\r\\n\\t\\treturn Result.ok(\\"删除成功\\");\\r\\n\\t}\\nvoid deleteCommentsByBlogId(Long blogId);\\nvoid deleteBlogTagByBlogId(Long blogId);\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\nvoid deleteBlogById(Long id);\\n"},{"name":"Blog_1444","description":"该Java类表示一个博客文章实体，包含文章的基本信息（如标题、内容、描述等）、状态开关（如是否公开、推荐、赞赏等）、时间戳、统计数据（如浏览次数、字数、阅读时长）以及与用户、分类和标签的关联。","content":"/**\\r\\n * @Description: 博客文章\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-26\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Blog {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String title;//文章标题\\r\\n\\tprivate String firstPicture;//文章首图，用于随机文章展示\\r\\n\\tprivate String content;//文章正文\\r\\n\\tprivate String description;//描述\\r\\n\\tprivate Boolean published;//公开或私密\\r\\n\\tprivate Boolean recommend;//推荐开关\\r\\n\\tprivate Boolean appreciation;//赞赏开关\\r\\n\\tprivate Boolean commentEnabled;//评论开关\\r\\n\\tprivate Boolean top;//是否置顶\\r\\n\\tprivate Date createTime;//创建时间\\r\\n\\tprivate Date updateTime;//更新时间\\r\\n\\tprivate Integer views;//浏览次数\\r\\n\\tprivate Integer words;//文章字数\\r\\n\\tprivate Integer readTime;//阅读时长(分钟)\\r\\n\\tprivate String password;//密码保护\\r\\n\\r\\n\\tprivate User user;//文章作者(因为是个人博客，也可以不加作者字段，暂且加上)\\r\\n\\tprivate Category category;//文章分类\\r\\n\\tprivate List<Tag> tags = new ArrayList<>();//文章标签\\r\\n}"},{"name":"Comment_1418","description":"该Java类Comment表示博客评论功能模块，用于存储和管理评论相关信息，包括评论内容、评论者信息、评论时间、所属文章以及回复关系等。","content":"/**\\r\\n * @Description: 博客评论\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-27\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Comment {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String nickname;//昵称\\r\\n\\tprivate String email;//邮箱\\r\\n\\tprivate String content;//评论内容\\r\\n\\tprivate String avatar;//头像(图片路径)\\r\\n\\tprivate Date createTime;//评论时间\\r\\n\\tprivate String website;//个人网站\\r\\n\\tprivate String ip;//评论者ip地址\\r\\n\\tprivate Boolean published;//公开或回收站\\r\\n\\tprivate Boolean adminComment;//博主回复\\r\\n\\tprivate Integer page;//0普通文章，1关于我页面\\r\\n\\tprivate Boolean notice;//接收邮件提醒\\r\\n\\tprivate Long parentCommentId;//父评论id\\r\\n\\tprivate String qq;//如果评论昵称为QQ号，则将昵称和头像置为QQ昵称和QQ头像，并将此字段置为QQ号备份\\r\\n\\r\\n\\tprivate BlogIdAndTitle blog;//所属的文章\\r\\n\\tprivate List<Comment> replyComments = new ArrayList<>();//回复该评论的评论\\r\\n}"},{"name":"Category_1542","description":"该代码定义了一个名为Category的Java类，用于表示博客分类。该类包含了分类的唯一标识(id)、分类名称(name)和一个存储该分类下所有博客文章(blogs)的列表。","content":"/**\\r\\n * @Description: 博客分类\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-26\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Category {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String name;//分类名称\\r\\n\\tprivate List<Blog> blogs = new ArrayList<>();//该分类下的博客文章\\r\\n}"}]

              **Requirement Description:**
              我想要能够更新博客的置顶文章的状态

              ### **Output Format (in Chinese):**
              Please return the breakdown results in the following JSON format:
              ```json
              {
                  "subtasks": [
                      {
                          "id": 1,
                          "name": "子任务1",
                          "description": "子任务1的详细描述"
                      },
                      {
                          "id": 2,
                          "name": "子任务2",
                          "description": "子任务2的详细描述"
                      }
                  ]
              }

              Ensure:
              - Output in Chinese
              - Each subtask focuses on a single functional component
              - No specific code details in the description of subtask
              - Excludes non-functional requirements
            """;

        long startTime1 = System.currentTimeMillis();
        System.out.println(llmService.generateAnswer(prompt));
        long endTime1 = System.currentTimeMillis();
        System.out.println("llmService.generateAnswer(prompt) 执行时间: " + (endTime1 - startTime1) + " ms");

        long startTime2 = System.currentTimeMillis();
        System.out.println(llm.chat(prompt));
        long endTime2 = System.currentTimeMillis();
        System.out.println("llm.chat(prompt) 执行时间: " + (endTime2 - startTime2) + " ms");
    }
}
