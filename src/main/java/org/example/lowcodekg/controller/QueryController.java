package org.example.lowcodekg.controller;

import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.service.processor.MainService;
import org.example.lowcodekg.query.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description 响应低代码模板资源推荐请求
 * @Author Sherloque
 * @Date 2025/3/21 19:51
 */
@RestController
public class QueryController {

    @Autowired
    private MainService mainService;

    @RequestMapping("/recommend")
    public Result<List<Node>> recommendResource(@RequestParam String query) {
        Result result = mainService.recommend(query);
        if(result.getCode() == ResultCodeEnum.SUCCESS.getCode()) {
            return Result.build(result.getData(), ResultCodeEnum.SUCCESS);
        }
        return Result.build(null, ResultCodeEnum.FAIL);
    }
}
