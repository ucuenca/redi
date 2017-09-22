/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.test.base.EmbeddedMarmotta;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public abstract class AbstractREDIMongoEnv {

    private static final MongodStarter starter = MongodStarter.getDefaultInstance();
    private static MongodExecutable _mongodExe;
    private static MongodProcess _mongod;
    private static MongoClient _mongo;
    private static EmbeddedMarmotta marmotta;
    public static int MONGO_PORT = 27016;
    public static String MONGO_HOST = "localhost";

    @BeforeClass
    public static void setUp() throws Exception {
        _mongodExe = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
                .build());
        _mongod = _mongodExe.start();
        _mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
        marmotta = new EmbeddedMarmotta();
        ConfigurationService configurationService = marmotta.getService(ConfigurationService.class);
        configurationService.setConfiguration("mongo.port", String.valueOf(MONGO_PORT));
        configurationService.setConfiguration("mongo.host", MONGO_HOST);
    }

    @Before
    public abstract void registerServices();

    @AfterClass
    public static void tearDown() throws Exception {
        _mongod.stop();
        _mongodExe.stop();
        marmotta.shutdown();
    }

    public Mongo getMongo() {
        return _mongo;
    }

    public EmbeddedMarmotta getMarmotta() {
        return marmotta;
    }

}
