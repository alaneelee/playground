package com.example.playground;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

@SpringBootTest
public class JedisTests {

    @Test
    public void jedisTest() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.set("foo", "bar");
            System.out.println(jedis.get("foo"));

            Map<String, String> hash = new HashMap<>();
            hash.put("name", "John");
            hash.put("surname", "Smith");
            hash.put("company", "Redis");
            hash.put("age", "29");
            jedis.hset("user-session:123", hash);
            System.out.println(jedis.hgetAll("user-session:123"));
        }
    }

    @Test
    public void jedisStringTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.set("users:100:email", "lee@gmail.com");
            jedis.set("users:100:name", "lee 00");
            jedis.set("users:100:age", "100");

            var userEmail = jedis.get("users:100:email");
            System.out.println(userEmail);

            List<String> userInfo = jedis.mget("users:100:email", "users:100:name",
                "users:100:age");
            userInfo.forEach(System.out::println);

            long counter = jedis.incr("counter");
            System.out.println(counter);

            counter = jedis.incrBy("counter", 10L);
            System.out.println(counter);

            counter = jedis.decr("counter");
            System.out.println(counter);

            counter = jedis.decrBy("counter", 10L);
            System.out.println(counter);

            Pipeline pipelined = jedis.pipelined();
            pipelined.set("users:400:email", "greg@gmail.com");
            pipelined.set("users:400:name", "greg");
            pipelined.set("users:400:age", "15");
            List<Object> objects = pipelined.syncAndReturnAll();
            objects.forEach(i -> System.out.println(i.toString()));
        }
    }

    @Test
    public void jedisListTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            // list
            // 1. stack
            jedis.rpush("stack1", "aaa");
            jedis.rpush("stack1", "bbb");
            jedis.rpush("stack1", "ccc");

            List<String> stack1 = jedis.lrange("stack1", 0, -1);
            stack1.forEach(System.out::println);

            System.out.println(jedis.rpop("stack1"));
            System.out.println(jedis.rpop("stack1"));
            System.out.println(jedis.rpop("stack1"));
            // 2. queue
            jedis.rpush("queue2", "zz");
            jedis.rpush("queue2", "aa");
            jedis.rpush("queue2", "cc");

            System.out.println(jedis.lpop("queue2"));
            System.out.println(jedis.lpop("queue2"));
            System.out.println(jedis.lpop("queue2"));

            // 3. block brpop, blpop
            while (true) {
                List<String> blpop = jedis.blpop(10, "queue:blocking");
                if (blpop != null) {
                    blpop.forEach(System.out::println);
                }
            }
        }
    }

    @Test
    public void jedisSetTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.sadd("users:500:follow", "100", "200", "300");
            jedis.srem("users:500:follow", "100");

            Set<String> smembers = jedis.smembers("users:500:follow");
            smembers.forEach(System.out::println);

            System.out.println(jedis.sismember("users:500:follow", "200"));
            System.out.println(jedis.sismember("users:500:follow", "100"));

            System.out.println(jedis.scard("users:500:follow"));

            System.out.println(jedis.sadd("users:100:follow", "100","200"));
            System.out.println(jedis.sinter("users:100:follow", "users:500:follow"));
        }
    }

}
