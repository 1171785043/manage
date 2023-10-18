package com.example.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.system.common.constant.CommonConstant;
import com.example.system.common.vo.Result;
import com.example.system.entity.SysUser;
import com.example.system.mapper.SysUserMapper;
import com.example.system.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements
    SysUserService {

  /**
   * 校验用户是否有效
   *
   * @param sysUser 用户对象
   * @return 用户是否有效
   */
  @Override
  public Result<?> checkUserIsEffective(SysUser sysUser) {
    Result<?> result = new Result<>();
    //情况1：根据用户信息查询，该用户不存在
    if (sysUser == null) {
      result.error500("该用户不存在，请注册");
      log.warn("该用户不存在");
      return result;
    }
    //情况2：根据用户信息查询，该用户已注销
    if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
      result.error500("该用户已注销");
      log.warn("用户登录失败，用户名:" + sysUser.getUsername() + "已注销！");
      return result;
    }
    //情况3：根据用户信息查询，该用户已冻结
    if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
      result.error500("该用户已冻结");
      log.warn("用户登录失败，用户名:" + sysUser.getUsername() + "已冻结！");
      return result;
    }
    return result;
  }
}
