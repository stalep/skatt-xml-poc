package no.skatt.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import on.skatt.parser.FileParser;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
@Path("files.html")
public class FileController {

    @Location("files.html")
    Template template;

    @Inject
    FileParser parser;

    @GET
    public TemplateInstance get() {
        return template.instance();
    }

    @POST
    @Blocking
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TemplateInstance upload( @RestForm FileUpload file)  {
        System.out.println(">>>>");
        System.out.println("file:"+file.fileName());
        System.out.println("<<<<<");
        parser.parseFile(file.uploadedFile().toFile());

        return template.instance();
    }

}
