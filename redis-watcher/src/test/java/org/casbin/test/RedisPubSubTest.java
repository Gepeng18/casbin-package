package org.casbin.test;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.main.SyncedEnforcer;
import org.casbin.watcher.RedisWatcher;
import org.junit.Test;

public class RedisPubSubTest {

    @Test
    public void produce() {
        String redisTopic="jcasbin-topic";
        RedisWatcher redisWatcher = new RedisWatcher("127.0.0.1",6379, redisTopic);
        // Support for connecting to redis with timeout and password
        // RedisWatcher redisWatcher = new RedisWatcher("127.0.0.1",6379, redisTopic, 2000, "foobared");

        Enforcer enforcer = new SyncedEnforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        // 1、设置 redisWatcher 为监听器，将 this::loadPolicy 设置到监听器中，一旦接收到信号，会调用该监听器，即重新调用 this.adapter.loadPolicy(this.model);
        enforcer.setWatcher(redisWatcher);

        // The following code is not necessary and generally does not need to be written unless you understand what you want to do
        /*
        Runnable updateCallback = ()->{
            // Custom behavior
        };

        redisWatcher.setUpdateCallback(updateCallback);
        */

        // Modify policy, it will notify B
        enforcer.addPolicy("data2_admin", "data2", "write");
        // 2、主动调用 update() -> publish一波
        redisWatcher.update();
    }

    @Test
    public void consumer() throws InterruptedException {
        String redisTopic="jcasbin-topic";
        RedisWatcher redisWatcher = new RedisWatcher("127.0.0.1",6379, redisTopic);

        Enforcer enforcer = new SyncedEnforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        // 1、设置 redisWatcher 为监听器，将 this::loadPolicy 设置到监听器中，一旦接收到信号，会调用该监听器，即重新调用 this.adapter.loadPolicy(this.model);
        enforcer.setWatcher(redisWatcher);
        while (true) {
            Thread.sleep(10000);
            System.out.println(enforcer);
        }
        // B set watcher and subscribe redisTopic, then it will receive the notification of A, and then call LoadPolicy to reload policy
    }
}
