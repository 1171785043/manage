package com.example.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.system.common.constant.CommonConstant;
import com.example.system.common.model.SysLoginModel;
import com.example.system.common.util.Md5Util;
import com.example.system.common.util.PasswordUtil;
import com.example.system.common.util.RandImageUtil;
import com.example.system.common.vo.Result;
import com.example.system.config.BaseConfig;
import com.example.system.entity.SysUser;
import com.example.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys")
@Api(tags = "用户登录")
@Slf4j
public class LoginController {
  @Autowired
  private SysUserService sysUserService;

  @Autowired
  private BaseConfig baseConfig;

  @Resource
  private RedisTemplate<Object, Object> redisTemplate;

  /**
   * 登录
   *
   * @param sysLoginModel 登录请求对象
   * @return result
   */
  @PostMapping("/login")
  public Result<JSONObject> login(@RequestBody SysLoginModel sysLoginModel) {

    Result<JSONObject> result = new Result<>();
    String username = sysLoginModel.getUsername();
    String password = sysLoginModel.getPassword();

    if(isLoginFailOvertimes(username)){
      return result.error500("该用户登录失败次数过多，请于10分钟后再次登录！");
    }

    LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(SysUser::getUsername, username);
    SysUser sysUser = sysUserService.getOne(queryWrapper);

    // 校验用户是否有效
    result = sysUserService.checkUserIsEffective(sysUser);
    if(!result.isSuccess()) {
      return result;
    }

    // 校验验证码
    String captcha = sysLoginModel.getCaptcha();
    if (captcha == null) {
      result.error500("验证码无效");
      return result;
    }
    String lowerCaseCaptcha = captcha.toLowerCase();
    // 加入密钥作为混淆，避免简单的拼接，被外部利用，用户自定义该密钥即可
    String origin =
        lowerCaseCaptcha + sysLoginModel.getCheckKey() + baseConfig.getSignatureSecret();

    String realKey = Md5Util.md5Encode(origin, "utf-8");
    Object checkCode = redisTemplate.opsForValue().get(realKey);

    if (ObjectUtil.isNull(checkCode) || !checkCode.toString().equals(lowerCaseCaptcha)) {
      log.warn("验证码错误，key= {} , Ui checkCode= {}, Redis checkCode = {}",
          sysLoginModel.getCheckKey(), lowerCaseCaptcha, checkCode);
      result.error500("验证码错误");
      // 改成特殊的code 便于前端判断
      result.setCode(HttpStatus.PRECONDITION_FAILED.value());
      return result;
    }

    // 获取加密后的密码
    String userPassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
    String sysPassword = sysUser.getPassword();
    if (sysPassword.equals(userPassword) && username.equals(sysUser.getUsername())) {
      StpUtil.login(sysUser.getId());
      // 登录成功删除redis中的验证码
      redisTemplate.delete(realKey);
      result.success("登录成功");
      return result;
    } else {
      addLoginFailOvertimes(username);
      return result.error500("用户名或密码错误");
    }
  }

  /**
   * 退出
   *
   * @return result
   */
  @PostMapping("/logout")
  public Result<Object> logout() {
    StpUtil.logout();
    return Result.ok("退出成功");
  }

  /**
   * 判断当前账号是否登录
   *
   * @return result
   */
  @PostMapping("/isLogin")
  public Result<Object> isLogin() {
    boolean login = StpUtil.isLogin();
    return Result.ok(login);
  }

  /**
   * 后台生成图形验证码 ：有效
   *
   * @param key 生成一串数字作为密钥
   */
  @ApiOperation("获取验证码")
  @GetMapping(value = "/randomImage/{key}")
  public Result<String> randomImage(@PathVariable("key") String key) {
    Result<String> res = new Result<>();
    try {
      String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";
      // 生成验证码
      String code = RandomUtil.randomString(BASE_CHECK_CODES, 4);
      String lowerCaseCode = code.toLowerCase();

      // 加入密钥作为混淆，避免简单的拼接，被外部利用，用户自定义该密钥即可
      String origin = lowerCaseCode + key + baseConfig.getSignatureSecret();
      String realKey = Md5Util.md5Encode(origin, "utf-8");

      // 验证码设置60秒过期在redis中
      redisTemplate.opsForValue().set(realKey, lowerCaseCode, 300, TimeUnit.SECONDS);
      log.info("获取验证码，Redis key = {}，checkCode = {}", realKey, code);
      // 返回前端
      String base64 = RandImageUtil.generate(code);
      res.setSuccess(true);
      res.setResult(base64);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      res.error500("获取验证码失败,请检查redis配置!");
      return res;
    }
    return res;
  }

  /**
   * 登录失败超出次数5 返回true
   * @param username 用户名
   * @return 登录次数超过5 true  否则 false
   */
  private boolean isLoginFailOvertimes(String username){
    String key = CommonConstant.LOGIN_FAIL + username;
    Object failTime = redisTemplate.opsForValue().get(key);
    if(ObjectUtil.isNotNull(failTime)){
      assert failTime != null;
      int val = Integer.parseInt(failTime.toString());
      return val >= 5;
    }
    return false;
  }

  /**
   * 记录登录失败次数以及时间
   *
   * @param username 用户名
   */
  private void addLoginFailOvertimes(String username){
    String key = CommonConstant.LOGIN_FAIL + username;
    Object failTime = redisTemplate.opsForValue().get(key);
    int val = 0;
    if(failTime!=null){
      val = Integer.parseInt(failTime.toString());
    }

    // 1小时
    redisTemplate.opsForValue().set(key, String.valueOf(++val), 600, TimeUnit.SECONDS);
  }

}
