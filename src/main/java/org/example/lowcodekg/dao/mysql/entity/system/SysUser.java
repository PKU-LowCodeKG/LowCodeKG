package org.example.lowcodekg.dao.mysql.entity.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.lowcodekg.dao.mysql.entity.base.BaseEntity;

@Data
@Schema(description = "system user entity class")
public class SysUser extends BaseEntity {
    @Schema(description = "username")
    private String userName;

    @Schema(description = "password")
    private String password;

    @Schema(description = "email of the user")
    private String email;
}
