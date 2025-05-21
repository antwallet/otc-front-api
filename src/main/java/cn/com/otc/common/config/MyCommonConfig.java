package cn.com.otc.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/6/14
 */
@Data
@Configuration
public class MyCommonConfig {
    @Value("${robotname}")
    private String robotname;
    @Value("${otc.name}")
    private String redpacketName;

    @Value("${otc.version}")
    private String redpacketVersion;

    @Value("${common.token.secret}")
    private String commonTokenSecret;

    @Value("${common.token.timeout}")
    private Long commonTokenTimeOut;

    @Value("${common.logo.default-path}")
    private String commonLogoDefaultPath;

    @Value("${common.logo.trx-path}")
    private String commonLogoTrxPath;

    @Value("${common.logo.usdt-path}")
    private String commonLogoUsdtPath;

    @Value("${common.redpacket-cover.default-path}")
    private String commonRedpacketCoverDefaultPath;

    @Value("${common.robot-avatar.avatar-path}")
    private String commonRobotAvatarAvatarPath;

    @Value("${common.premium.type}")
    private String commonPremiumType;

    @Value("${common.administrator-tgId}")
    private String commonAdministratorTgId; //管理员tgId


    @Value("${tg.http.url}")
    private String tgHttpUrl;

    @Value("${tg.http.timeout}")
    private Integer tgHttpTimeOut;

    @Value("${tg.http.antwalletbot}")
    private String tgHttpAntwalletbot;

    @Value("${tg.http.channelBotName}")
    private String tgHttpChannelBotName;

    @Value("${tg.http.channelName}")
    private String tgHttpChannelName;

    @Value("${tg.http.isChannelNewMember}")
    private String tgHttpisChannelNewMember;

    @Value("${tg.http.tg-token}")
    private String tgHttpTgToken;


    @Value("${tg.http.api-file_path}")
    private String tgHttpApiFilePath;

    @Value("${api.signature.secret}")
    private String apiSignatureSecret;

    @Value("${api.antwalletbotasset.url}")
    private String apiAntwalletbotassetUrl;

    @Value("${api.antwalletbot.httpurl}")
    private String apiAntwalletbotHttpUrl;

    @Value("${api.antwalletgroup.httpurl}")
    private String apiAntwalletgroupHttpUrl;

    @Value("${cloudFlare.secretKey}")
    private String cloudFlareSecretKey;

    @Value("${tron.config.domainOnline}")
    private Boolean tronDomainOnline;

    @Value("${tron.config.apiKey}")
    private String tronApiKey;

    @Value("${tron.config.trc20Address}")
    private String trc20Address;

    @Value("${tron.config.hexPrivateKey}")
    private String hexPrivateKey;

    @Value("${tron.config.toAddress}")
    private String toAddress;

    @Value("${tron.config.withdrawAddress}")
    private String withdrawAddress;

    @Value("${tron.config.withdrawHexPrivateKey}")
    private String withdrawHexPrivateKey;

    @Value("${tron.config.energy.api-key}")
    private String energyApiKey;

    @Value("${tron.config.energy.api-secret}")
    private String energyApiSecret;

     /**
     * oss认证id
     */
    @Value("${oss.accessKey.id}")
    private String ossAccessKeyId;
    /**
     * oss认证密钥
     */
    @Value("${oss.accessKey.secret}")
    private String ossAccessKeySecret;
    @Value("${oss.endpoint}")
    private String ossEndpoint;

    @Value("${ton.config.path}")
    private String tonConfigPath;

    @Value("${whitelistPath}")
    private String whitelistPath;

    /**
     * 项目域名
     */
    @Value("${projectDomainName}")
    private String projectDomainName;
}
