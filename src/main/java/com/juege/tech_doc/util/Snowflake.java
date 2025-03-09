package com.juege.tech_doc.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.date.SystemClock;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Twitter的分布式自增ID雪花算法
 **/
@Slf4j
public class Snowflake {

	public static long MAX_START_INTERVAL_TIME = TimeUnit.SECONDS.toNanos(5);

	/**
	 * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）2025-01-01 00:00:00
	 */
	private static final long twepoch = 1735660800000L;

	/**
	 * 机器标识位数
	 */
	private final long workerIdBits = 5L;

	private final long datacenterIdBits = 5L;

	private final long maxWorkerId = ~(-1L << workerIdBits);

	private final long maxDatacenterId = ~(-1L << datacenterIdBits);

	/**
	 * 毫秒内自增位
	 */
	private final long sequenceBits = 12L;

	private final long workerIdShift = sequenceBits;

	private final long datacenterIdShift = sequenceBits + workerIdBits;

	/**
	 * 时间戳左移动位
	 */
	private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

	private final long sequenceMask = ~(-1L << sequenceBits);

	private final long workerId;

	/**
	 * 数据标识 ID 部分
	 */
	private final long datacenterId;

	/**
	 * 并发控制
	 */
	private long sequence = 0L;

	/**
	 * 上次生产 ID 时间戳
	 */
	private long lastTimestamp = -1L;

	/**
	 * IP 地址
	 */
	private InetAddress inetAddress;

	public Snowflake(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
		long start = System.nanoTime();
		this.datacenterId = getDatacenterId(maxDatacenterId);
		this.workerId = getMaxWorkerId(datacenterId, maxWorkerId);
		long end = System.nanoTime();
		if (end - start > Snowflake.MAX_START_INTERVAL_TIME) {
			// 一般这里启动慢,是未指定inetAddress时出现,请查看本机hostname,将本机hostname写入至本地系统hosts文件之中进行解析
			log.warn("Initialization Sequence Very Slow! Get datacenterId:" + this.datacenterId + " workerId:" + this.workerId);
		}
		else {
			initLog();
		}
	}

	private void initLog() {
		if (log.isDebugEnabled()) {
			log.debug("Initialization Sequence datacenterId:" + this.datacenterId + " workerId:" + this.workerId);
		}
	}

	/**
	 * 有参构造器
	 *
	 * @param workerId     工作机器 ID
	 * @param datacenterId 序列号
	 */
	public Snowflake(long workerId, long datacenterId) {
		Assert.isFalse(workerId > maxWorkerId || workerId < 0,
				String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		Assert.isFalse(datacenterId > maxDatacenterId || datacenterId < 0,
				String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		initLog();
	}

	/**
	 * 获取 maxWorkerId
	 */
	protected long getMaxWorkerId(long datacenterId, long maxWorkerId) {
		StringBuilder mpid = new StringBuilder();
		mpid.append(datacenterId);
		String name = ManagementFactory.getRuntimeMXBean().getName();
		if (StrUtil.isNotBlank(name)) {
			/*
			 * GET jvmPid
			 */
			mpid.append(name.split(StrPool.AT)[0]);
		}
		/*
		 * MAC + PID 的 hashcode 获取16个低位
		 */
		return (mpid.toString().hashCode() & 0xffff) % (maxWorkerId + 1);
	}

	/**
	 * 数据标识id部分
	 */
	protected long getDatacenterId(long maxDatacenterId) {
		long id = 0L;
		try {
			if (null == this.inetAddress) {
				if (log.isDebugEnabled()) {
					log.debug("Use localhost address ");
				}
				this.inetAddress = InetAddress.getLocalHost();
			}
			if (log.isDebugEnabled()) {
				log.debug("Get " + inetAddress + " network interface ");
			}
			NetworkInterface network = NetworkInterface.getByInetAddress(this.inetAddress);
			if (log.isDebugEnabled()) {
				log.debug("Get network interface info: " + network);
			}
			if (null == network) {
				log.warn("Unable to get network interface for " + inetAddress);
				id = 1L;
			}
			else {
				byte[] mac = network.getHardwareAddress();
				if (null != mac) {
					id = ((0x000000FF & (long) mac[mac.length - 2]) | (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
					id = id % (maxDatacenterId + 1);
				}
			}
		}
		catch (Exception e) {
			log.warn(" getDatacenterId: " + e.getMessage());
		}
		return id;
	}

	/**
	 * 获取下一个 ID
	 *
	 * @return 下一个 ID
	 */
	public synchronized long nextId() {
		long timestamp = timeGen();
		//闰秒
		if (timestamp < lastTimestamp) {
			long offset = lastTimestamp - timestamp;
			if (offset <= 5) {
				try {
					wait(offset << 1);
					timestamp = timeGen();
					if (timestamp < lastTimestamp) {
						throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", offset));
			}
		}

		if (lastTimestamp == timestamp) {
			// 相同毫秒内，序列号自增
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				// 同一毫秒的序列数已经达到最大
				timestamp = tilNextMillis(lastTimestamp);
			}
		}
		else {
			// 不同毫秒内，序列号置为 1 - 2 随机数
			sequence = ThreadLocalRandom.current().nextLong(1, 3);
		}

		lastTimestamp = timestamp;

		// 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
		return ((timestamp - twepoch) << timestampLeftShift)
				| (datacenterId << datacenterIdShift)
				| (workerId << workerIdShift)
				| sequence;
	}

	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	protected long timeGen() {
		return SystemClock.now();
	}

	/**
	 * 反解id的时间戳部分
	 */
	public static long parseIdTimestamp(long id) {
		return (id >> 22) + twepoch;
	}
}
