package com.atguigu.gmall.index;

import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallIndexApplicationTests {

	@Autowired
	RedissonClient redissonClient;
	@Test
	void contextLoads() {
		RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("bloomFilter");
		bloomFilter.tryInit(20,0.3);
		System.out.println(bloomFilter.add("1"));
		System.out.println(bloomFilter.add("2"));
		System.out.println(bloomFilter.add("3"));
		System.out.println(bloomFilter.add("4"));
		System.out.println(bloomFilter.add("5"));
		System.out.println(bloomFilter.add("6"));
		System.out.println("-----------------------------------");
		System.out.println(bloomFilter.contains("7"));
		System.out.println(bloomFilter.contains("8"));
		System.out.println(bloomFilter.contains("9"));
		System.out.println(bloomFilter.contains("10"));
		System.out.println(bloomFilter.contains("11"));
		System.out.println(bloomFilter.contains("12"));
		System.out.println(bloomFilter.contains("13"));
		System.out.println(bloomFilter.contains("14"));
		System.out.println(bloomFilter.contains("19"));
		System.out.println(bloomFilter.contains("15"));
		System.out.println(bloomFilter.contains("16"));
		System.out.println(bloomFilter.contains("17"));
	}

}
