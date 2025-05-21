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
 * @description:钱包地址池表
 * @author: zhangliyan
 * @time: 2024/4/1
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_wallet_pool")
public class TWalletPool implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * 主键
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * base58 钱包地址
   */
  @TableField(value = "base58_check_address")
  private String base58CheckAddress;

  /**
   * 钱包地址是否锁定 0,未锁定 1,已锁定
   */
  @TableField("islock")
  private Integer islock;

  /**
   * 钱包地址是否禁用 0,未禁用 1,已禁用
   */
  @TableField("isdel")
  private Integer isdel;

  /**
   * 钱包私钥
   */
  @TableField("private_key")
  private String privateKey;

  /**
   * 创建时间
   */
  @TableField("create_time")
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  @TableField("update_time")
  private LocalDateTime updateTime;

}
