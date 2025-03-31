package org.example.lowcodekg.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class PromptTest {
    @Autowired
    private LLMGenerateService llmService;

    @Test
    void test() {
        String prompt = """
                You are an expert in Software Engineering and have extensive experience in understanding complex software projects and analyzing source code.
                Your task is to decompose a given task description into multiple subtasks based on the potentially relevant code snippets and corresponding description provided.
                You should focus on the split point of the function, for example, for a “user login” query, the specific split should be the user to fill in the login information, send a login request, user information verification, return to the login success of such a process.
                
                The code content is:
                [{"name":"updateTop_292","description":"该代码片段定义了一个用于更新博客置顶状态的HTTP PUT请求处理方法。通过接收博客ID和是否置顶的状态参数，调用blogService中的updateBlogTopById方法来更新数据库中的博客置顶信息，并返回一个操作成功的Result对象。","content":"/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\nvoid updateBlogTopById(Long blogId, Boolean top);\\n"},{"name":"updateTop_1866","description":"该代码片段定义了一个用于更新博客置顶状态的HTTP PUT请求处理方法。通过接收博客ID和是否置顶的状态参数，调用blogService中的updateBlogTopById方法来更新数据库中的博客置顶信息，并返回一个操作成功的Result对象。","content":"/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\n/**\\r\\n\\t * 更新博客置顶状态\\r\\n\\t *\\r\\n\\t * @param id  博客id\\r\\n\\t * @param top 是否置顶\\r\\n\\t * @return\\r\\n\\t */\\r\\n\\t@OperationLogger(\\"更新博客置顶状态\\")\\r\\n\\t@PutMapping(\\"/blog/top\\")\\r\\n\\tpublic Result updateTop(@RequestParam Long id, @RequestParam Boolean top) {\\r\\n\\t\\tblogService.updateBlogTopById(id, top);\\r\\n\\t\\treturn Result.ok(\\"操作成功\\");\\r\\n\\t}\\npublic static Result ok(String msg) {\\r\\n\\t\\treturn new Result(200, msg);\\r\\n\\t}\\nvoid updateBlogTopById(Long blogId, Boolean top);\\n"},{"name":"Comment_1594","description":"定义了一个博客评论类，包含评论的基本信息（如昵称、邮箱、内容等），评论的状态（是否公开、是否为博主回复），以及与之相关的文章和回复评论列表。","content":"/**\\r\\n * @Description: 博客评论\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-27\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Comment {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String nickname;//昵称\\r\\n\\tprivate String email;//邮箱\\r\\n\\tprivate String content;//评论内容\\r\\n\\tprivate String avatar;//头像(图片路径)\\r\\n\\tprivate Date createTime;//评论时间\\r\\n\\tprivate String website;//个人网站\\r\\n\\tprivate String ip;//评论者ip地址\\r\\n\\tprivate Boolean published;//公开或回收站\\r\\n\\tprivate Boolean adminComment;//博主回复\\r\\n\\tprivate Integer page;//0普通文章，1关于我页面\\r\\n\\tprivate Boolean notice;//接收邮件提醒\\r\\n\\tprivate Long parentCommentId;//父评论id\\r\\n\\tprivate String qq;//如果评论昵称为QQ号，则将昵称和头像置为QQ昵称和QQ头像，并将此字段置为QQ号备份\\r\\n\\r\\n\\tprivate BlogIdAndTitle blog;//所属的文章\\r\\n\\tprivate List<Comment> replyComments = new ArrayList<>();//回复该评论的评论\\r\\n}"},{"name":"Category_1718","description":"定义了一个名为Category的Java类，用于表示博客分类。该类包含一个唯一的标识符id、分类名称name以及属于该分类的博客文章列表blogs。","content":"/**\\r\\n * @Description: 博客分类\\r\\n * @Author: Naccl\\r\\n * @Date: 2020-07-26\\r\\n */\\r\\n@NoArgsConstructor\\r\\n@Getter\\r\\n@Setter\\r\\n@ToString\\r\\npublic class Category {\\r\\n\\tprivate Long id;\\r\\n\\tprivate String name;//分类名称\\r\\n\\tprivate List<Blog> blogs = new ArrayList<>();//该分类下的博客文章\\r\\n}"}]
                
                The task description is:
                我想要能够更新博客的置顶文章的状态
                
                Please return the subtasks in the following JSON format:
                ```json
                {
                    "subtasks": [
                        {
                            "id": 1,
                            "name": "subtask1",
                            "description": "subtask1 description"
                        },
                        {
                            "id": 2,
                            "name": "subtask2",
                            "description": "subtask2 description"
                        }
                    ]
                }
                ```
                
                Note that the result should be in Chinese.
                """;

        String answer = llmService.generateAnswer(prompt);

        System.out.println(answer);
    }

    @Test
    void testJson() {
        String str = """
                {
                   "reserved_resources": [
                     {
                       "name": "Blog_1620"
                     },
                     {
                       "name": "VisitLog_1773"
                     }
                   ]
                }
                """;
        JSONObject jsonObject = JSONObject.parseObject(str);
        JSONArray jsonArray = jsonObject.getJSONArray("reserved_resources");

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            String taskName = item.getString("name");
            System.out.println(taskName);
        }
    }
}
