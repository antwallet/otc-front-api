package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description:用户充值表
 * @author: zhangliyan
 * @time: 2024/4/1
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_charge")
public class TCharge implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * 主键
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 订单ID
   */
  @TableField(value = "order_id")
  private String orderId;

  /**
   * 用户ID
   */
  @TableField("user_id")
  private String userId;

  /**
   * base58 钱包地址
   */
  @TableField("base58_check_address")
  private String base58CheckAddress;

  /**
   * 充值状态 0,未充值 1,已充值 2 已过期
   */
  @TableField("status")
  private Integer status;

  /**
   * 创建时间
   */
  @TableField("create_time")
  private LocalDateTime createTime;

  /**
   * 有效时间
   */
  @TableField("expire_time")
  private LocalDateTime expireTime;

  /**
   * 更新时间
   */
  @TableField("update_time")
  private LocalDateTime updateTime;

  /**
   * 充值金额
   */
  @TableField("money")
  private String money;

  /**
   *充值类型
   */
  @TableField("charge_type")
  private Integer chargeType;

  /**
   * 充值时间
   */
  @TableField("charge_time")
  private LocalDateTime chargeTime;

  /**
   * 充值txid
   */
  @TableField("charge_txid")
  private String chargeTxid;

  /**
   * 充值二维码地址
   */
  @TableField("qrcode_image")
  private String qrcodeImage;

}
