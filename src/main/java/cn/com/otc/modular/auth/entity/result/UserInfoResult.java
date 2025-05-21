package cn.com.otc.modular.auth.entity.result;

import lombok.Data;

/**
 * @description:返回的用户信息bean
 * @author: zhangliyan
 * @time: 2024/2/21
 */
@Data
public class UserInfoResult {

  /**
   * 用户tgid
   */
  private String userTGID;
  /**
   * 用户id
   */
  private String userId;

  /**
   * 昵称
   */
  private String firstName;

  /**
   * 用户名
   */
  private String userName;

  /**
   * 头像
   */
  private String avatar;

  /**
   * 红包封面
   */
  private String redpacketCoverImg;

  /**
   * 是否设置了支付密码
   */
  private boolean isSetChargePsw;

  /**
   * 是否展示弹窗 0,弹出 1,不弹
   */
  private boolean isshowPanel;
}
