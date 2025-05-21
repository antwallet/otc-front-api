package cn.com.otc.sso.controller;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.sso.constant.SaOAuth2Consts;
import cn.com.otc.sso.constant.SaOAuth2Consts.Api;
import cn.com.otc.sso.service.OAuth2OidcService;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2Config;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Consts.Param;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Consts.ResponseType;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Handle;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Template;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Util;
import cn.dev33.satoken.oauth2.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.model.CodeModel;
import cn.dev33.satoken.oauth2.model.RequestAuthModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sa-OAuth2 Server端 控制器
 * @author zhangliyan
 * 
 */
@Slf4j
@RestController
public class SsoServerOAuth2Controller {
	@Autowired
	private OAuth2OidcService oAuth2OidcService;
	@Autowired
	HttpServletRequest httpRequest;
	@Autowired
	private MyCommonConfig myCommonConfig;

	public static SaOAuth2Template saOAuth2Template = new SaOAuth2Template();

	// 处理所有OAuth相关请求
	@RequestMapping(SaOAuth2Consts.Api.oAuth2PathPreFix)
	public Object request(HttpServletRequest request) {
		return serverRequest();
	}

	/**
	 * 按照OAuth2 规范，将Sa-Token的返回值转换为OAuth2的返回值
	 * doc: https://developer.baidu.com/wiki/connect/index.php?title=%E7%99%BE%E5%BA%A6OAuth2.0%E9%94%99%E8%AF%AF%E5%93%8D%E5%BA%94
	 * @param saResult
	 * @return
	 */
	public ResponseEntity<?> transformToOAuth2 (SaResult saResult) {
		Map<String, Object> map = new HashMap<>();
		if (saResult.getCode() != 200) {
			return jsonError(String.valueOf(saResult.getCode()), saResult.getMsg());
		} else {
			return ResponseEntity.ok().body(saResult.getData());
		}
	}

	public Object serverRequest() {
		SaRequest req = SaHolder.getRequest();
		SaResponse res = SaHolder.getResponse();
		SaOAuth2Config cfg = SaOAuth2Manager.getConfig();
		// ------------------ 路由分发 ------------------
		// Refresh-Token 刷新 Access-Token
		if(req.isPath(Api.oAuth2Refresh) && req.isParam(SaOAuth2Consts.Param.grant_type, SaOAuth2Consts.GrantType.refresh_token)) {
			return transformToOAuth2((SaResult) SaOAuth2Handle.refreshToken(req));
		}

		// 回收 Access-Token
		if(req.isPath(Api.oAuth2Revoke)) {
			return SaOAuth2Handle.revokeToken(req);
		}

		// doConfirm 确认授权接口
		if(req.isPath(Api.oAuth2DoConfirm)) {
			return SaOAuth2Handle.doConfirm(req);
		}

		// 模式二：隐藏式
		if(req.isPath(Api.oAuth2Authorize) && req.isParam(SaOAuth2Consts.Param.response_type, SaOAuth2Consts.ResponseType.token) && cfg.isImplicit) {
			return authorize(req, res, cfg);
		}

		// 模式三：密码式
		if(req.isPath(Api.oAuth2Token) && req.isParam(SaOAuth2Consts.Param.grant_type, SaOAuth2Consts.GrantType.password) && cfg.isPassword) {
			return SaOAuth2Handle.password(req, res, cfg);
		}

		// 模式四：凭证式
		if(req.isPath(Api.oAuth2ClientToken) && req.isParam(SaOAuth2Consts.Param.grant_type, SaOAuth2Consts.GrantType.client_credentials) && cfg.isClient) {
			return SaOAuth2Handle.clientToken(req, res, cfg);
		}
		// oidc 接口
		if (req.isPath(Api.OIDC_METADATA_ENDPOINT)) {
			String urlPrefix =  req.getUrl();
			String pathUrl = getReqUrl();
			log.info("正在进行OIDC_METADATA_ENDPOINT操作,获取的urlPrefix={} currDomain={} pathUrl={}",urlPrefix, SaManager.getConfig().getCurrDomain(), pathUrl);
			urlPrefix = urlPrefix.replace(Api.OIDC_METADATA_ENDPOINT, "");
			Map<String, Object> m = oAuth2OidcService.metadata(urlPrefix);
			return ResponseEntity.ok().body(m);
		}
		if (req.isPath(Api.OIDC_INTROSPECTION_ENDPOINT)) {
			String accessToken = getAccessToken();
			Map<String, Object> m = oAuth2OidcService.introspection(accessToken);
			return ResponseEntity.ok().body(m);

		}
		if (req.isPath(Api.OIDC_JWKS_ENDPOINT)) {
			String jwks =  oAuth2OidcService.jwks();
			return ResponseEntity.ok().body(jwks);
		}

		// 默认返回
		return SaOAuth2Consts.NOT_HANDLE;
	}


	public Object authorize(SaRequest req, SaResponse res, SaOAuth2Config cfg) {

		// 1、如果尚未登录, 则先去登录
		if(StpUtil.isLogin() == false) {
			return cfg.notLoginView.get();
		}

		// 2、构建请求Model
		RequestAuthModel ra = SaOAuth2Util.generateRequestAuth(req, StpUtil.getLoginId());

		// 3、校验：重定向域名是否合法
		SaOAuth2Util.checkRightUrl(ra.clientId, ra.redirectUri);

		// 4、校验：此次申请的Scope，该Client是否已经签约
		SaOAuth2Util.checkContract(ra.clientId, ra.scope);

		// 5、判断：如果此次申请的Scope，该用户尚未授权，则转到授权页面
		boolean isGrant = SaOAuth2Util.isGrant(ra.loginId, ra.clientId, ra.scope);
		if(isGrant == false) {
			return cfg.confirmView.apply(ra.clientId, ra.scope);
		}

		// 7、判断授权类型
		// 如果是 授权码式，则：开始重定向授权，下放code
		if(ResponseType.code.equals(ra.responseType)) {
			CodeModel codeModel = SaOAuth2Util.generateCode(ra);
			String redirectUri = SaOAuth2Util.buildRedirectUri(ra.redirectUri, codeModel.code, ra.state);
			return res.redirect(redirectUri);
		}
		// 如果是 隐藏式，则：开始重定向授权，下放 token
		if(ResponseType.token.equals(ra.responseType)) {
			AccessTokenModel at = SaOAuth2Util.generateAccessToken(ra, false);
			String redirectUri = SaOAuth2Util.buildImplicitRedirectUri(ra.redirectUri, at.accessToken, ra.state);
			return res.redirect(redirectUri);
		}

		// 默认返回
		throw new SaOAuth2Exception("无效response_type: " + ra.responseType);
	}

	@RequestMapping("/doorgod_oauth2_server/authorize")
	public Object saOAuth2Authorize(){
		SaRequest req = SaHolder.getRequest();
		SaResponse res = SaHolder.getResponse();
		SaOAuth2Config cfg = SaOAuth2Manager.getConfig();
		// 模式一：Code授权码
		if(req.isParam(SaOAuth2Consts.Param.response_type, SaOAuth2Consts.ResponseType.code) && cfg.isCode) {
			String clientId = req.getParam("client_id");
			Object r = SaOAuth2Handle.authorize(req, res, cfg);
			if (StpUtil.isLogin()) {
				String nonce = req.getParam("nonce");
				String state = req.getParam("state");
				if (StrUtil.isNotEmpty(nonce)) {
					// 这是一段特殊兼容代码，用于兼容OIDC的nonce
					// md5(state,clientId)为key，记住nonce  这个本来是用code值来存的，但是外部方法没办法拿到内部的code值，所以只能用state和client_id来做key
					oAuth2OidcService.saveNonce(nonce, state, clientId);
				}
			}
			return r;
		}
		// 默认返回
		return SaOAuth2Consts.NOT_HANDLE;
	}

	/**
	 * Code授权码 获取 Access-Token
	 * @return 处理结果
	 */
	@RequestMapping("/doorgod_oauth2_server/token")
	public Object saOAuth2Token(){
		SaRequest req = SaHolder.getRequest();
		SaResponse res = SaHolder.getResponse();
		SaOAuth2Config cfg = SaOAuth2Manager.getConfig();
		// Code授权码 获取 Access-Token
		if(req.isParam(SaOAuth2Consts.Param.grant_type, SaOAuth2Consts.GrantType.authorization_code)) {
			try {
				log.info("Code授权码 开始获取 Access-Token");
				String code = req.getParamNotNull(Param.code);
				String clientId1 = req.getParamNotNull(Param.client_id);
				String clientSecret = req.getParamNotNull(Param.client_secret);
				String redirectUri = req.getParam(Param.redirect_uri);
				log.info("Code授权码 获取 Access-Token code={},clientId={},clientSecret={},redirectUri={}", code,clientId1,clientSecret,redirectUri);
				SaResult saResult = (SaResult) token(req, res, cfg);
				log.info("Code授权码 获取 Access-Token saResult=[{}]", JSONUtil.toJsonStr(saResult));
				if (saResult.getCode() != 200) {
					return transformToOAuth2(saResult);
				}
				Map<String, Object> map = (Map<String, Object>) saResult.getData();
				map.put("token_type", "Bearer");
				// id_token  oidc协议
				String access_token = map.get("access_token").toString();
				if (access_token == null) {
					throw new RRException("access_token is null");
				}
				try {
					Object loginId = SaOAuth2Util.getLoginIdByAccessToken(access_token);
					SaOAuth2Util.checkScope(access_token, "openid");
					String iss = req.getUrl().replace(Api.oAuth2Token, "");
					String state = req.getParam("state");
					String clientId = req.getParam("client_id");
					String nonce = oAuth2OidcService.getNonce(state, clientId);
					map.put("id_token", oAuth2OidcService.createIdToken(access_token, iss, nonce));
					map.put("login_id", loginId);
				} catch (Exception e) {
					throw e;
				}
				log.info("Code授权码 获取 Access-Token map=[{}]", JSONUtil.toJsonStr(map));
				return transformToOAuth2(SaResult.data(map));
			}catch (Exception e) {
				log.error("授权码验证失败", e);
				return transformToOAuth2(SaResult.error("授权码验证失败，有可能是授权码已过期"));
			}
		}
		// 默认返回
		return SaOAuth2Consts.NOT_HANDLE;
	}

	public static Object token(SaRequest req, SaResponse res, SaOAuth2Config cfg)
			throws Exception {
		// 获取参数
		String code = req.getParamNotNull(Param.code);
		String clientId = req.getParamNotNull(Param.client_id);
		String clientSecret = req.getParamNotNull(Param.client_secret);
		String redirectUri = req.getParam(Param.redirect_uri);

		// 校验参数
		SaOAuth2Util.checkGainTokenParam(code, clientId, clientSecret, null);

		//如果提供了redirectUri，则校验其是否与请求Code时提供的一致
		if(SaFoxUtil.isNotEmpty(redirectUri)) {
			CodeModel cm = saOAuth2Template.getCode(code);
			// 只校验host就行了
			URL u1;
			URL u2;
			try {
				String url1 = cm.getRedirectUri();
				String url2 = redirectUri;
				u1 = new URL(url1);
				u2 = new URL(url2);
			} catch (Exception e) {
				log.warn("无效redirect_uri，格式异常", e);
				throw new SaOAuth2Exception("无效redirect_uri，格式异常");
			}
			String host1 = u1.getHost();
			String host2 = u2.getHost();
			SaOAuth2Exception.throwBy(!host1.equals(host2), "无效redirect_uri: " + redirectUri);
		}

		// 构建 Access-Token
		AccessTokenModel token = SaOAuth2Util.generateAccessToken(code);

		// 返回
		return SaResult.data(token.toLineMap());
	}

	private String getAccessToken () {
		// 从header中获取，需要去除内容的前缀 Bearer
		String accessToken = SaHolder.getRequest().getHeader("Authorization");
		if (accessToken != null && accessToken.startsWith("Bearer ")) {
			accessToken = accessToken.substring(7);
		}
		if (accessToken == null) {
			// 从参数中获取
			accessToken = SaHolder.getRequest().getParam("access_token");
		}
		return accessToken;
	}

	private  String getReqUrl(){
		// 获取协议
		String protocol = httpRequest.getScheme();

    // 获取主机名
		String hostname = httpRequest.getServerName();

    // 获取端口
		int port = httpRequest.getServerPort();

    // 获取路径
		String path = httpRequest.getRequestURI();

    // 构建完整的请求 URL
		String url = protocol + "://" + hostname + ":" + port + path;
		return url;
	}

	// 全局异常拦截
	@ExceptionHandler(Exception.class)
	public Object handlerException(Exception e) {
		log.error("出现了异常，请检查", e);
		return transformToOAuth2(SaResult.error("系统未知异常，请联系管理员"));
	}
	@ExceptionHandler(SaOAuth2Exception.class)
	public Object handlerException(SaOAuth2Exception e) {
		log.warn("SaOAuth2Exception业务异常，请检查", e);
		return transformToOAuth2(SaResult.error(e.getMessage()));
	}
	@ExceptionHandler(RRException.class)
	public SaResult handlerException(RRException e) {
		log.warn("业务异常，请检查", e);
		return SaResult.get(e.getCode(), e.getMsg(), null);
	}
	private static String urlencode(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s, String.valueOf(StandardCharsets.UTF_8));
	}

	private static ResponseEntity<String> response401() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.TEXT_HTML);
		responseHeaders.add("WWW-Authenticate", "Basic realm=\"OIDC server\"");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body("<html><body><h1>401 Unauthorized</h1>OIDC server</body></html>");
	}

	private static ResponseEntity<?> jsonError(String error, String error_description) {
		log.warn("error={} error_description={}", error, error_description);
		Map<String, String> map = new LinkedHashMap<>();
		map.put("error", error);
		map.put("error_description", error_description);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	}
}
