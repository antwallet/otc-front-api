package cn.com.otc.sso.service;

import cn.com.otc.modular.sys.bean.pojo.TChannelConfig;
import cn.com.otc.modular.sys.service.TChannelConfigService;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Template;
import cn.dev33.satoken.oauth2.model.SaClientModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sa-Token OAuth2.0 整合实现 
 * @author zhangliyan
 */
@Slf4j
@Component
public class SaOAuth2TemplateImpl extends SaOAuth2Template {

	@Autowired
	private TChannelConfigService tChannelConfigService;
	
	// 根据 id 获取 Client 信息 
	@Override
	public SaClientModel getClientModel(String clientId) {
		   LambdaQueryWrapper<TChannelConfig> lambdaQueryWrapper = new LambdaQueryWrapper<>();
		   lambdaQueryWrapper.eq(TChannelConfig::getChannelId,clientId);
		   lambdaQueryWrapper.eq(TChannelConfig::getIslock,0);
		   TChannelConfig tChannelConfig = tChannelConfigService.getOne(lambdaQueryWrapper);
		   if(tChannelConfig ==null){
          return null;
			 }
			// 常用的scope: openid profile email phone address
			// scope分隔符 我们这里用逗号  RFC 6749 中并没有具体规定多个 scope 参数值的分隔符，建议使用空格或者逗号
			return new SaClientModel()
					.setClientId(clientId)
					.setClientSecret(tChannelConfig.getChannelSecret())
					.setAllowUrl("*")
					.setContractScope("openid,profile");
	}
	// -------------- 其它需要重写的函数 
	
}
