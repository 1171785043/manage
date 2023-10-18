package com.example.system.common.util;

import java.util.Random;

public class ConvertUtil {

  /**
   * 随机数
   * @param place 定义随机数的位数
   */
  public static String randomGen(int place) {
    String base = "qwertyuioplkjhgfdsazxcvbnmQAZWSXEDCRFVTGBYHNUJMIKLOP0123456789";
    StringBuffer stringBuffer = new StringBuffer();
    Random rd = new Random();
    for(int i=0;i<place;i++) {
      stringBuffer.append(base.charAt(rd.nextInt(base.length())));
    }
    return stringBuffer.toString();
  }

  public static int convertSex(String sex) {
    if ("男".equals(sex)) {
      return 1;
    } else if ("女".equals(sex)) {
      return 2;
    } else {
      throw new IllegalArgumentException("性别请填写 男 或 女");
    }
  }
}
