package com.juege.tech_doc.controller;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.juege.tech_doc.domain.User;
import com.juege.tech_doc.enums.LongPollStatusEnum;
import com.juege.tech_doc.req.UserLoginReq;
import com.juege.tech_doc.req.UserQueryReq;
import com.juege.tech_doc.req.UserResetPasswordReq;
import com.juege.tech_doc.req.UserSaveReq;
import com.juege.tech_doc.resp.CommonResp;
import com.juege.tech_doc.resp.PageResp;
import com.juege.tech_doc.resp.UserLoginResp;
import com.juege.tech_doc.resp.UserQueryResp;
import com.juege.tech_doc.service.UserService;
import com.juege.tech_doc.util.LoginUserContext;
import com.juege.tech_doc.util.Snowflake;
import io.github.linpeilie.Converter;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/user")
public class UserController {

	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	@Resource
	private UserService userService;

	@Resource
	private Snowflake snowFlake;

	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private Converter converter;

	@GetMapping("/list")
	public CommonResp list(@Valid UserQueryReq req) {
		CommonResp<PageResp<UserQueryResp>> resp = new CommonResp<>();
		PageResp<UserQueryResp> list = userService.list(req);
		resp.setContent(list);
		return resp;
	}

	@PostMapping("/save")
	public CommonResp save(@Valid @RequestBody UserSaveReq req) {
		req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
		CommonResp resp = new CommonResp<>();
		userService.save(req);
		return resp;
	}

	@DeleteMapping("/delete/{id}")
	public CommonResp delete(@PathVariable Long id) {
		CommonResp resp = new CommonResp<>();
		userService.delete(id);
		return resp;
	}

	@PostMapping("/reset-password")
	public CommonResp resetPassword(@Valid @RequestBody UserResetPasswordReq req) {
		req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
		CommonResp resp = new CommonResp<>();
		userService.resetPassword(req);
		return resp;
	}

	@PostMapping("/login")
	public CommonResp login(@Valid @RequestBody UserLoginReq req) {
		req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
		CommonResp<UserLoginResp> resp = new CommonResp<>();
		UserLoginResp userLoginResp = userService.login(req);

		Long token = snowFlake.nextId();
		LOG.info("生成单点登录token：{}，并放入redis中", token);
		userLoginResp.setToken(token.toString());
		redisTemplate.opsForValue().set(token.toString(), JSONObject.toJSONString(userLoginResp), 3600 * 24, TimeUnit.SECONDS);

		resp.setContent(userLoginResp);
		return resp;
	}

	@GetMapping("/logout/{token}")
	public CommonResp logout(@PathVariable String token) {
		CommonResp resp = new CommonResp<>();
		redisTemplate.delete(token);
		LOG.info("从redis中删除token: {}", token);
		return resp;
	}

	@GetMapping("/get-current-login")
	public CommonResp<UserLoginResp> getCurrentLogin() {
		final User dbUser = userService.getCurrentLogin();
		final UserLoginResp user = LoginUserContext.getUser();
		final UserLoginResp loginUserInfoResp = converter.convert(dbUser, UserLoginResp.class);
		loginUserInfoResp.setToken(user.getToken());
		return CommonResp.success(loginUserInfoResp);
	}

	@GetMapping("/long-poll")
	public DeferredResult<CommonResp<String>> longPoll() {
		final DeferredResult<CommonResp<String>> deferredResult = new DeferredResult<>(30000L);
		final UserLoginResp user = LoginUserContext.getUser();
		// 超时处理：返回空响应
		deferredResult.onTimeout(() -> {
			LoginUserContext.removeDeferredResult(user.getId(), user.getToken(), deferredResult);
			deferredResult.setResult(CommonResp.success(LongPollStatusEnum.TIMEOUT.name()));
		});

		// 完成处理：清理队列
		deferredResult.onCompletion(() -> LoginUserContext.removeDeferredResult(user.getId(), user.getToken(), deferredResult));
		// 加入等待队列
		LoginUserContext.addDeferredResult(user.getId(), user.getToken(), deferredResult);
		return deferredResult;
	}
}
