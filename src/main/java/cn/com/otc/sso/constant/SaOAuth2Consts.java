package cn.com.otc.sso.constant;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/8/4
 */
public class SaOAuth2Consts {
  public static final class Api {
    // ==========OAuth2 server端接口 =================
    public static final String oAuth2PathPreFix = "/antwalletbot_oauth2_server/**/*";
    public static final String oAuth2Authorize = "/antwalletbot_oauth2_server/authorize";
    public static final String oAuth2Token = "/antwalletbot_oauth2_server/token";
    public static final String oAuth2Refresh = "/antwalletbot_oauth2_server/refresh";
    public static final String oAuth2Revoke = "/antwalletbot_oauth2_server/revoke";
    public static final String oAuth2ClientToken = "/antwalletbot_oauth2_server/client_token";
    public static final String oAuth2DoConfirm = "/antwalletbot_oauth2_server/doConfirm";
    /**
     * 用户信息接口
     */
    public static final String oAuth2UserInfo = "/antwalletbot_oauth2_server/userinfo";
    /**
     * METADATA_ENDPOINT: 该端点向客户端公开了OIDC提供的一些关键信息，例如发行人(Issuer)、授权端点(Authorization Endpoint)、用户信息端点(UserInfo Endpoint)、密钥(JWKS)等。客户端可以使用这些信息来进行OIDC认证流程。
     */
    public static final String OIDC_METADATA_ENDPOINT = "/antwalletbot_oauth2_server/.well-known/openid-configuration";
    /**
     * 该端点包含了OIDC提供的公钥(Key)和证书(Certificate)，这些Key和Certificate是用于数字签名和加密ID Token和AccessToken的关键信息。
     */
    public static final String OIDC_JWKS_ENDPOINT = "/antwalletbot_oauth2_server/jwks";
    /**
     * 该端点用于验证和解密AccessToken和ID Tokens，并明确已授权的作用域和权限。该端点通常供OIDC客户端和OIDC认证服务器使用，以了解您已签发的访问令牌的有效性和范围。
     */
    public static final String OIDC_INTROSPECTION_ENDPOINT = "/antwalletbot_oauth2_server/introspect";

  }

  /**
   * 所有参数名称
   * @author kong
   */
  public static final class Param {
    public static String response_type = "response_type";
    public static String client_id = "client_id";
    public static String client_secret = "client_secret";
    public static String redirect_uri = "redirect_uri";
    public static String scope = "scope";
    public static String state = "state";
    public static String code = "code";
    public static String token = "token";
    public static String access_token = "access_token";
    public static String refresh_token = "refresh_token";
    public static String grant_type = "grant_type";
    public static String username = "username";
    public static String password = "password";
    public static String name = "name";
    public static String pwd = "pwd";
  }

  /**
   * 所有返回类型
   */
  public static final class ResponseType {
    public static String code = "code";
    public static String token = "token";
  }

  /**
   * 所有授权类型
   */
  public static final class GrantType {
    public static String authorization_code = "authorization_code";
    public static String refresh_token = "refresh_token";
    public static String password = "password";
    public static String client_credentials = "client_credentials";
  }

  /** 表示OK的返回结果 */
  public static final String OK = "ok";

  /** 表示请求没有得到任何有效处理 {msg: "not handle"} */
  public static final String NOT_HANDLE = "{\"msg\": \"not handle\"}";
}
