package org.example.lowcodekg.model.vo.workspace;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "response entity when getting a workspace by id")
public class WorkspaceVo {
    private Long id;

    @Schema(description = "workspaceName")
    private String workspaceName;

    @Schema(description = "用户聊天历史记录")
    private String messages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private Date updatedAt;
}
