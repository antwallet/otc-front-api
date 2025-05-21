/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package cn.com.otc.common.exception;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * 异常处理器
 */
@Slf4j
@RestControllerAdvice
public class RRExceptionHandler {

	/**
	 * 处理自定义异常
	 */
	@ExceptionHandler(RRException.class)
	public R handleRRException(RRException e){
		R r = new R();
		r.put("code", e.getCode());
		r.put("msg", e.getMessage());

		return r;
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public R handlerNoFoundException(Exception e) {
		log.error(e.getMessage(), e);
		return R.error(404, "路径不存在，请检查路径是否正确");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public R handleDuplicateKeyException(DuplicateKeyException e){
		log.error(e.getMessage(), e);
		return R.error("数据库中已存在该记录");
	}

	@ExceptionHandler({NumberFormatException.class})    //申明捕获那个异常类
	public String NumberFormatExceptionDemo(Exception e) {
		log.error(e.getMessage(), e);
		return "数字转换异常返回";
	}

	@ExceptionHandler(RuntimeException.class)
	public R handleException(Exception e){
		log.error(e.getMessage(), e);
		//String trackId = LogInterceptor.getTrackId();
		//return R.error("未知异常 trackId：" + trackId);
		return R.error("未知异常");
	}

	/**
	 * 参数效验异常处理器
	 * @param e 参数验证异常
	 * @return ResponseInfo
	 */
	//@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public ResponseEntity<?> parameterExceptionHandler(MethodArgumentNotValidException e) {
		log.error("参数效验异常处理器", e);
		// 获取异常信息
		BindingResult exceptions = e.getBindingResult();
		// 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
		if (exceptions.hasErrors()) {
			List<ObjectError> errors = exceptions.getAllErrors();
			if (!errors.isEmpty()) {
				// 这里列出了全部错误参数，按正常逻辑，只需要第一条错误即可
				FieldError fieldError = (FieldError) errors.get(0);
				return ResponseEntity.failure(fieldError.getDefaultMessage());
			}
		}
		return ResponseEntity.failure("参数校验失败");
	}
}
