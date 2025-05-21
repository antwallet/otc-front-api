package cn.com.otc.modular.auth.entity.vo;

import lombok.Data;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/22
 */
@Data
public class LoginUserVO {

  private String userTGID;
  private String firstName;
  private String userName;
  private String data;
  private Boolean isPremium;
  private String deviceModel;
  private String registrationDate;
  private String resolution;
  private String networkType;
  private String networkStatus;
}
