package no.skatt.parser;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.mutiny.amqp.AmqpClient;
import io.vertx.mutiny.amqp.AmqpConnection;
import io.vertx.mutiny.amqp.AmqpMessage;
import io.vertx.mutiny.amqp.AmqpReceiver;
import no.skatt.controller.FileController;
import on.skatt.parser.FileParser;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FileController.class)
public class FileParserTest {

    @ConfigProperty(name = "amqp-host") String host;
    @ConfigProperty(name = "amqp-port") int port;
    private AmqpClient client;

    @BeforeEach
    void setUp() {
        client = AmqpClient.create(new AmqpClientOptions().setHost(host).setPort(port));
    }

    @AfterEach
    void tearDown() {
        client.closeAndAwait();
    }

    @Test
    public void testParser() {
        AmqpConnection connection = client.connectAndAwait();
        AmqpReceiver quotes = connection.createReceiverAndAwait("tasks");
        AssertSubscriber<AmqpMessage> subscriber = quotes.toMulti().subscribe().withSubscriber(AssertSubscriber.create(Long.MAX_VALUE));
       given().multiPart("file",new File(
                Thread.currentThread().getContextClassLoader()
                        .getResource("data/saldorente_v3_eksempel_normal.xml").getFile()) )
                        .expect().statusCode(200).when().post();

       subscriber.awaitItems(5);
        AmqpMessage received = subscriber.getItems().get(0);
        assertEquals(received.bodyAsJsonObject().getString("deposit"), "50569");
    }

    @Test
    @Disabled //Only for testing processing speed
    public void testBigFile() {
        FileParser parser = new FileParser();
        parser.parseFile(new File(
                Thread.currentThread().getContextClassLoader()
                        .getResource("data/saldorente_v3_eksempel_verybig.xml").getFile()));

    }
}
