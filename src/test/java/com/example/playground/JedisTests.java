package com.example.playground;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.Tuple;

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

            System.out.println(jedis.sadd("users:100:follow", "100", "200"));
            System.out.println(jedis.sinter("users:100:follow", "users:500:follow"));
        }
    }

    @Test
    public void jedisHashTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.hset("users:1:info", "name", "alanee");

            var userInfo = new HashMap<String, String>();
            userInfo.put("email", "alanee@gmail.com");
            userInfo.put("phone", "010-0000-0000");

            jedis.hset("users:1:info", userInfo);
            System.out.println(jedis.hget("users:1:info", "phone"));

            jedis.hdel("users:1:info", "phone");
            System.out.println(jedis.hget("users:1:info", "phone"));

            jedis.hincrBy("users:1:info", "visits", 5);

            Map<String, String> getUserInfo = jedis.hgetAll("users:1:info");

            getUserInfo.forEach((k, v) -> System.out.printf("%s: %s%n", k, v));
        }
    }

    @Test
    public void jedisSortedSetTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            var scores = new HashMap<String, Double>();
            scores.put("users1", 100.0);
            scores.put("users2", 30.0);
            scores.put("users3", 50.0);
            scores.put("users4", 80.0);
            scores.put("users5", 15.0);

            jedis.zadd("game1:scores", scores);

            List<String> zrange = jedis.zrange("game1:scores", 0, Long.MAX_VALUE);
            zrange.forEach(System.out::println);

            System.out.println(jedis.zcard("game1:scores"));

            jedis.zincrby("game1:scores", 100.0, "users5");

            List<Tuple> tuples = jedis.zrangeWithScores("game1:scores", 0, Long.MAX_VALUE);
            tuples.forEach(i -> System.out.printf("%s: %s%n", i.getElement(), i.getScore()));
        }
    }

    @Test
    public void jedisGeospatialTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.geoadd("stores1:geo", 127.02985530619755, 37.49911212874, "some1");
            jedis.geoadd("stores1:geo", 127.0333352287619, 37.491921163986234, "some2");

            Double geodist = jedis.geodist("stores1:geo", "some1", "some2");
            System.out.println(geodist);

            List<GeoRadiusResponse> geoRadiusResponseList = jedis.geosearch("stores1:geo",
                new GeoSearchParam()
                    .fromLonLat(new GeoCoordinate(127.033, 37.495))
                    .byRadius(500, GeoUnit.M)
                    .withCoord()
            );

            geoRadiusResponseList.forEach(
                response -> System.out.printf("member: %s%nlat: %s%nlon: %s%n",
                    response.getMemberByString(), response.getCoordinate()
                        .getLatitude(), response.getCoordinate().getLongitude()));

            jedis.unlink("stores1:geo");

        }
    }

    @Test
    public void jedisBitmapTests() {
        JedisPool pool = new JedisPool("localhost", 6379);

        try (Jedis jedis = pool.getResource()) {
            jedis.setbit("request-somepage-0107", 100, true);
            jedis.setbit("request-somepage-0107", 200, true);
            jedis.setbit("request-somepage-0107", 300, true);

            System.out.println(jedis.getbit("request-somepage-0107", 100));
            System.out.println(jedis.getbit("request-somepage-0107", 50));

            System.out.println(jedis.bitcount("request-somepage-0107"));

            // bitmap vs set
            Pipeline pipelined = jedis.pipelined();
            IntStream.rangeClosed(0, 1000000).forEach(i -> {
                pipelined.sadd("request-somepage-set", String.valueOf(i), "1");
                pipelined.setbit("request-somepage-bit", i, true);

                if (i == 1000) {
                    pipelined.sync();
                }
            });
            pipelined.sync();
            // 터미널에서 MEMORY USAGE request-somepage-set 와 request-somepage-bit를 비교해보면 차이를 느낄 수 있다.
        }
    }
}
