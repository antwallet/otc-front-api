package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.I18nUtil;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TBindOtherUser;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.service.TBindOtherUserService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/5/9
 */
@Slf4j
@RestController
@RequestMapping("/api/front/bindotheruser")
public class TBindOtherUserController {
  @Autowired
  private TBindOtherUserService tBindOtherUserService;
  @Autowired
  private TUserService tUserService;
  @Autowired
  private CheckTokenUtil checkTokenUtil;

  /**
   * 获取绑定账户信息列表
   * @return
   */
  @RequestMapping("/list")
  public R list(HttpServletRequest httpRequest){
    try{
      /**
       * 1、根据token获取用户信息
       */
      String token = checkTokenUtil.getRequestToken(httpRequest);
      UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

      /**
       * 2、获取用户信息
       */
      TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
      if(tUser == null){
        log.info("TBindOtherUserController.list 用户信息不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
        return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code,"用户不存在哦,请选择提供正确的用户哦!");
      }

      /**
       * 3、获取绑定的用户
       */
      LambdaQueryWrapper<TBindOtherUser> lambdaQueryWrapper = new LambdaQueryWrapper<TBindOtherUser>();
      lambdaQueryWrapper.eq(TBindOtherUser::getUserId,tUser.getUserId());
      List<TBindOtherUser> list = tBindOtherUserService.list(lambdaQueryWrapper);
      for (TBindOtherUser tBindOtherUser:list) {
        tBindOtherUser.setBindUserInfo("("+tBindOtherUser.getBindTgid()+") "+tBindOtherUser.getBindName());
        tBindOtherUser.setBindNick("@"+tBindOtherUser.getBindNick());
      }

      return R.ok().put("list",list);
    }catch (Exception e){
      log.error("获取绑定账户信息列表失败,具体失败信息:",e);
      return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,"获取绑定账户信息列表失败,请联系管理员!");
    }
  }

  /**
   * 增加绑定账户
   * @return
   */
  @RequestMapping("/save")
  public R save(HttpServletRequest httpRequest,@RequestBody String tgId){
    try{
      if(StringUtils.isBlank(tgId)){
        log.warn("增加绑定账户失败,非法参数");
        return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1000",null));
      }
      /**
       * 1、根据token获取用户信息
       */
      String token = checkTokenUtil.getRequestToken(httpRequest);
      UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

      /**
       * 2、判断绑定用户和被绑定用户是否一样
       */
      if(userInfoResult.getUserTGID().equals(tgId)){
        log.warn("增加绑定账户失败,tgId={}和bindtgId={}一致",userInfoResult.getUserTGID(),tgId);
        return R.error(ResultCodeEnum.USER_BINDED_EEROR.code,"绑定用户和被绑定的用户一致,请检查");
      }

      /**
       * 3、获取用户信息
       */
      TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
      if(tUser == null){
        log.warn("用户不存在哦,请选择提供正确的用户哦,tgId={},bindtgId={}",userInfoResult.getUserTGID(),tgId);
        return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code,"用户不存在哦,请选择提供正确的用户哦!");
      }

      /**
       * 4、判断tgid是否存在
       */
      TUser bindTUser = tUserService.getTUserByTGId(tgId);
      if(bindTUser == null){
        log.warn("绑定用户不存在哦,请选择提供正确的用户哦,tgId={},bindtgId={}",userInfoResult.getUserTGID(),tgId);
        return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code,"绑定用户不存在哦,请选择提供正确的用户哦!");
      }

      /**
       * 5、查看绑定账户是否绑定了
       */
      LambdaQueryWrapper<TBindOtherUser> lambdaQueryWrapper = new LambdaQueryWrapper<TBindOtherUser>();
      lambdaQueryWrapper.eq(TBindOtherUser::getBindTgid,tgId);
      List<TBindOtherUser> tBindOtherUserList = tBindOtherUserService.list(lambdaQueryWrapper);
      if(tBindOtherUserList !=null && tBindOtherUserList.size() > 0){
        return R.error(ResultCodeEnum.USER_BINDED_EEROR.code,"绑定失败: 此账户已关联其他账户,请重新选择绑定的用户id!");
      }

      /**
       * 6、开始绑定
       */
      TBindOtherUser tBindOtherUserBean = new TBindOtherUser();
      tBindOtherUserBean.setUserId(tUser.getUserId());
      tBindOtherUserBean.setBindTgid(bindTUser.getTgId());
      tBindOtherUserBean.setBindName(bindTUser.getName());
      tBindOtherUserBean.setBindNick(bindTUser.getNick());
      tBindOtherUserBean.setCreateTime(LocalDateTime.now());
      tBindOtherUserService.save(tBindOtherUserBean);

      return R.ok();
    }catch (Exception e){
      log.error("绑定账户失败,具体失败信息:",e);
      return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,"绑定账户失败,请联系管理员!");
    }
  }


  /**
   * 删除绑定账户关系
   * @return
   */
  @RequestMapping("/delete")
  public R delete(HttpServletRequest httpRequest,@RequestBody String id){
    try{
      String lang = httpRequest.getHeader("lang");
      if (StringUtils.isEmpty(lang)) {
        lang = "en-US";
      }
      if(id == null){
        log.warn("删除绑定账户关系失败,非法参数");
        return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code,I18nUtil.getMessage("1000",lang));
      }

      /**
       * 2、开始删除绑定关系
       */
      tBindOtherUserService.removeById(id);

      return R.ok();
    }catch (Exception e){
      log.error(String.format("删除绑定关系失败,id={},具体失败信息:",id),e);
      return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,"删除绑定关系失败,请联系管理员!");
    }
  }

}
