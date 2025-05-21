package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 配置表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_antwalletbot_config")
public class TAntwalletConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * pKey
     */
    @TableId(value = "p_key", type = IdType.INPUT)
    private String pKey;

    /**
     * pValue
     */
    @TableField("p_value")
    private String pValue;

    /**
     * pDesc
     */
    @TableField("p_desc")
    private String pDesc;

}
