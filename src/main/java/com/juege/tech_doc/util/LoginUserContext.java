package com.juege.tech_doc.util;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.juege.tech_doc.resp.CommonResp;
import com.juege.tech_doc.resp.UserLoginResp;

import org.springframework.web.context.request.async.DeferredResult;

public class LoginUserContext implements Serializable {

	private static final ThreadLocal<UserLoginResp> user = new ThreadLocal<>();

	private final static Map<Long, Map<String, Queue<DeferredResult<CommonResp<String>>>>> LONG_POLL_USER_MAP = new ConcurrentHashMap<>();

	public static UserLoginResp getUser() {
		return user.get();
	}

	public static void setUser(UserLoginResp user) {
		LoginUserContext.user.set(user);
	}

	public static Map<Long, Map<String, Queue<DeferredResult<CommonResp<String>>>>> getLongPollUserMap(){
		return LONG_POLL_USER_MAP;
	}

	public static void addDeferredResult(Long userId, String token, DeferredResult<CommonResp<String>> deferredResult){
		final Map<String, Queue<DeferredResult<CommonResp<String>>>> deferredResultMap = LONG_POLL_USER_MAP.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
		final Queue<DeferredResult<CommonResp<String>>> deferredResults = deferredResultMap.computeIfAbsent(token, k -> new ConcurrentLinkedQueue<>());
		deferredResults.offer(deferredResult);
	}

	public static void removeDeferredResult(Long userId, String token, DeferredResult<CommonResp<String>> deferredResult){
		final Map<String, Queue<DeferredResult<CommonResp<String>>>> queueMap = LONG_POLL_USER_MAP.get(userId);
		Optional.ofNullable(queueMap)
				.flatMap(stringQueueMap -> Optional.ofNullable(stringQueueMap.get(token)))
				.ifPresent(queue -> queue.remove(deferredResult));
	}

}
