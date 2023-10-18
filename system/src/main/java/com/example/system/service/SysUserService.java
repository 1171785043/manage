package com.example.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.system.common.vo.Result;
import com.example.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
  /**
   * 校验用户是否有效
   * @param sysUser 用户对象
   * @return 是否有效
   */
  Result checkUserIsEffective(SysUser sysUser);
}
