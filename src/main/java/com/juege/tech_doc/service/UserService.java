package com.juege.tech_doc.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.juege.tech_doc.domain.User;
import com.juege.tech_doc.exception.BusinessException;
import com.juege.tech_doc.exception.BusinessExceptionCode;
import com.juege.tech_doc.mapper.UserMapper;
import com.juege.tech_doc.req.UserLoginReq;
import com.juege.tech_doc.req.UserQueryReq;
import com.juege.tech_doc.req.UserResetPasswordReq;
import com.juege.tech_doc.req.UserSaveReq;
import com.juege.tech_doc.resp.PageResp;
import com.juege.tech_doc.resp.UserLoginResp;
import com.juege.tech_doc.resp.UserQueryResp;
import com.juege.tech_doc.util.CopyUtil;
import com.juege.tech_doc.util.Snowflake;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Service
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@Resource
	private UserMapper userMapper;

	@Resource
	private Snowflake snowFlake;

	public PageResp<UserQueryResp> list(UserQueryReq req) {
		final LambdaQueryWrapper<User> userLambdaQueryWrapper = Wrappers.lambdaQuery(User.class)
				.eq(!ObjectUtils.isEmpty(req.getLoginName()), User::getLoginName, req.getLoginName());
		final Page<User> userPage = PageDTO.of(req.getPage(), req.getSize());
		userMapper.selectPage(userPage, userLambdaQueryWrapper);
		LOG.info("总行数：{}", userPage.getTotal());
		LOG.info("总页数：{}", userPage.getPages());

		List<UserQueryResp> list = CopyUtil.copyList(userPage.getRecords(), UserQueryResp.class);

		PageResp<UserQueryResp> pageResp = new PageResp<>();
		pageResp.setTotal(userPage.getTotal());
		pageResp.setList(list);

		return pageResp;
	}

	/**
	 * 保存
	 */
	public void save(UserSaveReq req) {
		User user = CopyUtil.copy(req, User.class);
		if (ObjectUtils.isEmpty(req.getId())) {
			User userDB = selectByLoginName(req.getLoginName());
			if (ObjectUtils.isEmpty(userDB)) {
				// 新增
				user.setId(snowFlake.nextId());
				userMapper.insert(user);
			}
			else {
				// 用户名已存在
				throw new BusinessException(BusinessExceptionCode.USER_LOGIN_NAME_EXIST);
			}
		}
		else {
			// 更新
			user.setLoginName(null);
			user.setPassword(null);
			userMapper.updateById(user);
		}
	}

	public void delete(Long id) {
		userMapper.deleteById(id);
	}

	public User selectByLoginName(String LoginName) {
		final LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
				.eq(User::getLoginName, LoginName);
		List<User> userList = userMapper.selectList(queryWrapper);
		if (CollectionUtils.isEmpty(userList)) {
			return null;
		}
		else {
			return userList.get(0);
		}
	}

	/**
	 * 修改密码
	 */
	public void resetPassword(UserResetPasswordReq req) {
		User user = CopyUtil.copy(req, User.class);
		userMapper.updateById(user);
	}

	/**
	 * 登录
	 */
	public UserLoginResp login(UserLoginReq req) {
		User userDb = selectByLoginName(req.getLoginName());
		if (ObjectUtils.isEmpty(userDb)) {
			// 用户名不存在
			LOG.info("用户名不存在, {}", req.getLoginName());
			throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
		}
		else {
			if (userDb.getPassword().equals(req.getPassword())) {
				// 登录成功
				UserLoginResp userLoginResp = CopyUtil.copy(userDb, UserLoginResp.class);
				return userLoginResp;
			}
			else {
				// 密码不对
				LOG.info("密码不对, 输入密码：{}, 数据库密码：{}", req.getPassword(), userDb.getPassword());
				throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
			}
		}
	}
}
