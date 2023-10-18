package com.example.system.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * 注册表单
 */
@Data
@ApiModel(value = "注册对象", description = "注册对象")
public class SysRegisterModel {
  @ApiModelProperty(value = "账号")
  private String username;
  @ApiModelProperty(value = "真实姓名")
  private String realName;
  @ApiModelProperty(value = "密码")
  private String password;
  @ApiModelProperty(value = "手机号")
  private String phoneNumber;
  @ApiModelProperty(value = "验证码")
  private String authCode;
  @ApiModelProperty(value = "性别")
  private String sex;
}
