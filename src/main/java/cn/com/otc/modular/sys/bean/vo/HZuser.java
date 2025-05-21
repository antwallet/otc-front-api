package cn.com.otc.modular.sys.bean.vo;

import lombok.Data;

@Data
public class HZuser {
    private boolean miniappUser;

    public HZuser(boolean miniappUser) {
        this.miniappUser = miniappUser;
    }
}
