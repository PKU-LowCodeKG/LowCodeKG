package org.example.lowcodekg.dao.mysql.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.lowcodekg.dao.mysql.entity.base.BaseEntity;

@Data
@Schema(description = "workspace entity")
public class Workspace extends BaseEntity {
    @Schema(description = "owner of the workspace")
    private Long userId;

    @Schema(description = "chat history of the user in this workspace")
    private String messages;

    @Schema(description = "保留字段，也许没啥用，文件存储用了minio")
    private String fileData;

    @Schema(description = "name of the workspace")
    private String workspaceName;

    public Workspace(Long userId, String messages, String workspaceName) {
        this.userId = userId;
        this.messages = messages;
        this.workspaceName = workspaceName;
    }
}
