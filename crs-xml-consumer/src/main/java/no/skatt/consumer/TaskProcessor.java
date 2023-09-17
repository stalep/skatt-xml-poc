package no.skatt.consumer;

import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import no.skatt.model.Task;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class TaskProcessor {

    private int counter;
    @Incoming("tasks")
    @Blocking // we're simulating that processing of this object is a long-running task
    public void process(JsonObject t) {
        Task task = t.mapTo(Task.class);
        System.out.println("Got a task to process: "+task);
    }

}
