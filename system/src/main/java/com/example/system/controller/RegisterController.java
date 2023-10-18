package com.example.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.system.common.constant.CommonConstant;
import com.example.system.common.model.SysRegisterModel;
import com.example.system.common.util.ConvertUtil;
import com.example.system.common.util.PasswordUtil;
import com.example.system.common.vo.Result;
import com.example.system.entity.SysUser;
import com.example.system.service.SysUserService;
import io.swagger.annotations.Api;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys")
@Api(tags = "用户注册")
@Slf4j
public class RegisterController {

  @Autowired
  private SysUserService sysUserService;

  @PostMapping("/register")
  public Result<Object> register(@RequestBody SysRegisterModel sysRegisterModel) {
    String username = sysRegisterModel.getUsername();
    String password = sysRegisterModel.getPassword();
    String salt = ConvertUtil.randomGen(8);
    // 获取加盐后的密码
    String saltPassword = PasswordUtil.encrypt(username, password, salt);
    SysUser sysUser = new SysUser();
    sysUser.setId(RandomUtil.randomString(32));
    sysUser.setPhone(sysRegisterModel.getPhoneNumber());
    sysUser.setUsername(username);
    sysUser.setRealname(sysRegisterModel.getRealName());
    sysUser.setPassword(saltPassword);
    sysUser.setSalt(salt);
    sysUser.setStatus(CommonConstant.USER_UNFREEZE);
    sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
    sysUser.setActivitiSync(CommonConstant.ACT_SYNC_0);

    // 如果是后台创建用户则增加创建人，否则创建人为自己
    if (StpUtil.isLogin()) {
      String loginId = StpUtil.getLoginId().toString();
      SysUser user = sysUserService.getById(loginId);
      sysUser.setCreateBy(user.getUsername());
    } else {
      sysUser.setCreateBy(username);
    }

    sysUser.setCreateTime(new Date());
    try {
      sysUser.setSex(ConvertUtil.convertSex(sysRegisterModel.getSex()));
    } catch (RuntimeException e) {
      return Result.error(e.getMessage());
    }

    boolean saveResult = sysUserService.save(sysUser);
    if (saveResult) {
      return Result.ok("注册成功");
    } else {
      return Result.ok("注册失败");
    }
  }
}
