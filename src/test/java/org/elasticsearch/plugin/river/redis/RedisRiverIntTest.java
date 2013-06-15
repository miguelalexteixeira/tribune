package org.elasticsearch.plugin.river.redis;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPerparer;
import org.elasticsearch.plugins.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.client.Requests.countRequest;
import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Stephen Samuel
 */
public class RedisRiverIntTest {

    private static Logger logger = LoggerFactory.getLogger(RedisRiverIntTest.class);

    private Node node;
    private String river = "redis-river-" + System.currentTimeMillis();
    private String index = "redis-index"; // needs to match settings in json
    private String channel = "int-test-channel"; // must match the settings in json
    private Jedis jedis;

    public void shutdown() {
        logger.debug("Shutdown elastic...");
        node.close();
        logger.debug("...goodbye elastic");
        logger.debug("Quitting jedis...");
        jedis.quit();
        logger.debug("...I'm a quitter");
    }

    @Before
    public void setupElasticAndRedis() throws Exception {

        Settings globalSettings = settingsBuilder().loadFromClasspath("settings.yml").build();
        String json = copyToStringFromClasspath("/simple-redis-river.json");
        Settings riverSettings = settingsBuilder().loadFromSource(json).build();

        String hostname = riverSettings.get("redis.hostname");
        int port = riverSettings.getAsInt("redis.port", 6379);
        logger.debug("Connecting to Redis [hostname={} port={}]...", hostname, port);
        jedis = new Jedis(hostname, port, 0);
        logger.debug("... connected");

        logger.debug("Starting local elastic...");
        Tuple<Settings, Environment> initialSettings = InternalSettingsPerparer.prepareSettings(globalSettings, true);
        PluginManager pluginManager = new PluginManager(initialSettings.v2(), null);

        if (!initialSettings.v2().configFile().exists()) {
            FileSystemUtils.mkdirs(initialSettings.v2().configFile());
        }

        if (!initialSettings.v2().logsFile().exists()) {
            FileSystemUtils.mkdirs(initialSettings.v2().logsFile());
        }

        node = nodeBuilder().local(true).settings(globalSettings).node();

        logger.info("Create river [{}]", river);
        node.client().prepareIndex("_river", river, "_meta").setSource(json).execute().actionGet();

        logger.debug("Running Cluster Health");
        ClusterHealthResponse clusterHealth = node.client().admin().cluster()
                .health(clusterHealthRequest().waitForGreenStatus())
                .actionGet();
        logger.info("Done Cluster Health, status " + clusterHealth.getStatus());

        GetResponse response = node.client().prepareGet("_river", river, "_meta").execute().actionGet();
        assertTrue(response.isExists());

        logger.debug("...elasticized ok");
    }

    @Test
    public void connectRiverAndSendMessages() throws InterruptedException {

        Thread.sleep(1000);

        logger.debug("Publishing message [channel={}]", channel);
        jedis.publish(channel, "my name is sammy");

        Thread.sleep(1000);

        CountResponse resp =
                node.client().count(countRequest(index).types(channel).query(fieldQuery("content", "sammy"))).actionGet();
        logger.debug("Count response: {}", resp.getCount());
        assertEquals(resp.getCount(), 1);

        resp = node.client().count(countRequest(index).types(channel).query(fieldQuery("content", "coldplay"))).actionGet();
        logger.debug("Count response: {}", resp.getCount());
        assertEquals(resp.getCount(), 0);

        logger.debug("Publishing message [channel={}]", channel);
        jedis.publish(channel, "watching coldplay live");

        Thread.sleep(1000);

        resp = node.client().count(countRequest(index).types(channel).query(fieldQuery("content", "coldplay"))).actionGet();
        logger.debug("Count response: {}", resp.getCount());
        assertEquals(resp.getCount(), 1);

        shutdown();
    }
}
