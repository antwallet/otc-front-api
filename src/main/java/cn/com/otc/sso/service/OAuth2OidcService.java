package cn.com.otc.sso.service;

import cn.com.otc.sso.constant.SaOAuth2Consts;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.oauth2.logic.SaOAuth2Util;
import cn.dev33.satoken.oauth2.model.AccessTokenModel;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.crypto.SecureUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * OAuth2 OIDC Service 处理相关逻辑
 * @Author: zhangliyan
 * @Date: 2024/6/18 10:57
 */
@Service
@Slf4j
public class OAuth2OidcService {

    private JWSSigner signer;
    private JWKSet publicJWKSet;
    private JWSHeader jwsHeader;

    @Autowired
    private Environment environment;
    private final ResourceLoader resourceLoader;

    public OAuth2OidcService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws IOException, ParseException, JOSEException {
        try {
            /*String json = environment.getProperty("sa-token.oauth2.oidc-jwks");
            log.info("initializing JWK  jwks={}", json.substring(0, 20));
            JWKSet jwkSet = JWKSet.parse(json);
            JWK key = jwkSet.getKeys().get(0);
            signer = new RSASSASigner((RSAKey) key);
            publicJWKSet = jwkSet.toPublicJWKSet();
            jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build();*/
        } catch (Exception e) {
            log.error("Error initializing JWK 请检查", e);
        }
    }

    /**
     * 提供 OIDC 元数据。 有关规范，请参阅 https://openid.net/specs/openid-connect-discovery-1_0.html
     * @param urlPrefix
     * @return
     */
    public Map<String, Object> metadata (String urlPrefix) {
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("issuer", urlPrefix); // REQUIRED
        m.put("authorization_endpoint", urlPrefix + SaOAuth2Consts.Api.oAuth2Authorize); // REQUIRED
        m.put("token_endpoint", urlPrefix + SaOAuth2Consts.Api.oAuth2Token); // REQUIRED unless only the Implicit Flow is used
        m.put("userinfo_endpoint", urlPrefix + SaOAuth2Consts.Api.oAuth2UserInfo); // RECOMMENDED
        m.put("jwks_uri", urlPrefix + SaOAuth2Consts.Api.OIDC_JWKS_ENDPOINT); // REQUIRED
        m.put("introspection_endpoint", urlPrefix + SaOAuth2Consts.Api.OIDC_INTROSPECTION_ENDPOINT);
        m.put("scopes_supported", Arrays.asList("openid", "profile", "email")); // RECOMMENDED
        m.put("response_types_supported", Arrays.asList("id_token token", "code")); // REQUIRED
        m.put("grant_types_supported", Arrays.asList("authorization_code", "implicit")); //OPTIONAL
        m.put("token_endpoint_auth_methods_supported", Arrays.asList("client_secret_post")); // OPTIONAL
        m.put("subject_types_supported", Arrays.asList("public")); // REQUIRED
        m.put("id_token_signing_alg_values_supported", Arrays.asList("RS256", "none")); // REQUIRED
        m.put("claims_supported", Arrays.asList("sub", "iss", "name", "family_name", "given_name", "preferred_username", "email"));
        m.put("code_challenge_methods_supported", Arrays.asList("plain", "S256")); // PKCE support advertised
        return m;
    }

    /**
     * JWKSet 包含用于签署 ID 令牌的密钥的公共部分。
     * @return
     */
    public String jwks () {
        return publicJWKSet.toString();
    }

    /**
     * 用于验证访问令牌的端点。
     * @param accessToken
     * @return
     */
    public Map<String, Object> introspection (String accessToken) {
        Map<String, Object> m = new LinkedHashMap<>();
        AccessTokenModel accessTokenInfo = SaOAuth2Util.getAccessToken(accessToken);
        if (accessTokenInfo == null) {
            log.error("token not found in memory: {}", accessToken);
            m.put("active", false);
        } else {
            // see https://tools.ietf.org/html/rfc7662#section-2.2 for all claims
            m.put("active", true);
            m.put("scope", accessTokenInfo.scope);
            m.put("client_id", accessTokenInfo.clientId);
            m.put("username", accessTokenInfo.loginId);
            m.put("token_type", "Bearer");
            m.put("exp", accessTokenInfo.expiresTime);
            m.put("sub", accessTokenInfo.openid);
            m.put("iss", "dg-auth");
        }

        return m;
    }

    public String createIdToken(String accessToken, String iss, String nonce) throws NoSuchAlgorithmException, JOSEException {
        AccessTokenModel accessTokenModel = SaOAuth2Util.checkAccessToken(accessToken);

        return createIdToken(String.valueOf(accessTokenModel.loginId),
                accessTokenModel.openid, accessTokenModel.clientId, nonce, accessToken, iss);
    }

    private String createIdToken(String loginid, String openid, String client_id, String nonce, String accessToken, String iss) throws NoSuchAlgorithmException, JOSEException {
        // compute at_hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();
        digest.update(accessToken.getBytes(StandardCharsets.UTF_8));
        byte[] hashBytes = digest.digest();
        byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
        Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);
        // create JWT claims
        Map<String, Object> extUser = new LinkedHashMap<>();
        extUser.put("username", loginid);
        extUser.put("displayName", loginid);
        extUser.put("status", "enabled");
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(openid)
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(new Date( System.currentTimeMillis() + (SaManager.getConfig().getTimeout() * 1000)))
                .jwtID(UUID.randomUUID().toString())
                .claim("nonce", nonce)
                .claim("at_hash", encodedHash.toString())
                .claim("user", extUser)
                .build();
        // create JWT token
        SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        myToken.sign(signer);
        return myToken.serialize();
    }


    // 创建一个lru缓存(使用hutool工具类)，用于保存nonce  2000个应该能满足需求了
    private static final LRUCache<String, String> NONCE_CACHE = new LRUCache<>(2000);
    public void saveNonce (String nonce, String state, String client_id) {
        // 使用hutool工具类， md5(state,clientId)为key，记住nonce
        String key = SecureUtil.md5(state + client_id);
        NONCE_CACHE.put(key, nonce);
        log.info("saveNonce: key={}, value={}", key, nonce);
    }
    public String getNonce (String state, String client_id) {
        String key = SecureUtil.md5(state + client_id);
        String value = NONCE_CACHE.get(key);
        log.info("getNonce: key={}, value={}", key, value);
        return value;
    }

}
