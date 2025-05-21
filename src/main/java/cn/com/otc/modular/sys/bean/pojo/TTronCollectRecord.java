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
 * <p>
 * tron链上金额归集记录表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_tron_collect")
public class TTronCollectRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 转出钱包地址
     */
    @TableField(value = "from_address")
    private String fromAddress;

    /**
     * 转入钱包地址
     */
    @TableField(value = "to_address")
    private String toAddress;

    /**
     * 交易类型 TRX/USDT
     */
    @TableField(value = "tron_type")
    private String tronType;

    /**
     * 归集金额
     */
    @TableField(value = "collect_money")
    private String collectMoney;

    /**
     * 归集批次
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 归集时间
     */
    @TableField(value = "collect_time")
    private String collectTime;

    /**
     * 归集状态
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 刷新状态
     */
    @TableField(value = "isrefresh")
    private Integer isrefresh;

    /**
     * 归集状态描述
     */
    @TableField(value = "desc")
    private String desc;

    /**
     * 归集txid
     */
    @TableField(value = "collect_txid")
    private String collectTxid;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

}
