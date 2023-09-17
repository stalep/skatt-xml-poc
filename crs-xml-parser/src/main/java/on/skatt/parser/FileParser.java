package on.skatt.parser;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.skatt.model.ContactInfo;
import no.skatt.model.Delivery;
import no.skatt.model.DeliveryOwner;
import no.skatt.model.Task;
import no.skatt.model.TaskOwner;
import no.skatt.model.TaskOwnerAddress;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@ApplicationScoped
public class FileParser {

    private static boolean debug = true;
    @Inject
    @Channel("tasks")
    @OnOverflow(value = OnOverflow.Strategy.UNBOUNDED_BUFFER)
    Emitter<Task> taskEmitter;

    private int counter;
    @Blocking
    public void parseFile(File file) {
        if(debug) {
            System.out.println("path = " + file);
            System.out.println("Free Memory \t Total Memory \t Max Memory");
            System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024) +
                    "Mb \t \t " + Runtime.getRuntime().totalMemory() / (1024 * 1024) +
                    "Mb \t \t " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "Mb");
        }
        long startTime = System.currentTimeMillis();
        counter = 0;
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            // https://rules.sonarsource.com/java/RSPEC-2755
            // prevent xxe
            xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            doParse( xmlInputFactory.createXMLStreamReader( new FileInputStream(file)));

            if(debug) {
                System.out.println("parsing took: " + (System.currentTimeMillis() - startTime));
                System.out.println("parsed " + counter+" number of tasks");
                System.out.println("Free Memory \t Total Memory \t Max Memory");
                System.out.println(Runtime.getRuntime().freeMemory() / (1024 * 1024) +
                        "Mb \t \t " + Runtime.getRuntime().totalMemory() / (1024 * 1024) +
                        "Mb \t \t " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "Mb");
            }
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void doParse(XMLStreamReader reader) throws XMLStreamException {
        //read melding
        parseMessage(reader);
        //parse oppgavegiver
        Delivery delivery = parseOwner(reader);
        //parse all oppgave elements
        parseTask(reader, delivery);
    }

    //parsing elements inside oppgaveeier
    private TaskOwner parseTaskOwner(XMLStreamReader reader) throws XMLStreamException {
        int eventType = reader.getEventType();
        TaskOwner owner = new TaskOwner();
        while (reader.hasNext()) {

            eventType = reader.next();

            if (eventType == XMLEvent.START_ELEMENT) {

                switch (reader.getName().getLocalPart()) {

                    case "organisasjonsnummer":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.orgNumber = reader.getText();
                        }
                        break;

                    case "organisasjonsnavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.orgName = reader.getText();
                        }
                        break;

                    case "alternativIdentifikator":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.alternativeIdentifier = reader.getText();
                        }
                        break;

                    case "sektorkode":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.sectorCode = reader.getText();
                        }
                        break;

                    case "foedselsnummer":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.nationalIdentityNumber = reader.getText();
                        }
                        break;

                    case "fornavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.firstName = reader.getText();
                        }
                        break;

                    case "etternavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.lastName = reader.getText();
                        }
                        break;

                    case "mellomnavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.middleName = reader.getText();
                        }
                        break;

                    case "adresse":
                        owner.address = parseTaskOwnerAddress(reader);
                }
            }
            if (eventType == XMLEvent.END_ELEMENT) {
                // if </staff>
                if (reader.getName().getLocalPart().equals("oppgaveeier")) {
                    return owner;
                }
            }
        }
        throw new XMLStreamException("Did not find expected element oppgaveeier");
    }

    private TaskOwnerAddress parseTaskOwnerAddress(XMLStreamReader reader) throws XMLStreamException {
        int eventType = reader.getEventType();
        TaskOwnerAddress address = new TaskOwnerAddress();
        while (reader.hasNext()) {
            eventType = reader.next();
            if (eventType == XMLEvent.START_ELEMENT) {

                switch (reader.getName().getLocalPart()) {
                    case "landkode":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            address.countryCode = reader.getText();
                        }
                        break;
                    case "adressenavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            address.streetName = reader.getText();
                        }
                        break;
                    case "postkode":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            address.zipCode = reader.getText();
                        }
                        break;
                    case "byEllerStedsnavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            address.city = reader.getText();
                        }
                        break;
                }
            }
            if (eventType == XMLEvent.END_ELEMENT) {
                if (reader.getName().getLocalPart().equals("adresse")) {
                    return address;
                }
            }
        }
        throw new XMLStreamException("Did not find expected element adresse");
    }

    private void parseTask(XMLStreamReader reader, Delivery delivery) throws XMLStreamException {
        int eventType;

        Task task = new Task();
        while (reader.hasNext()) {
            eventType = reader.next();
            if (eventType == XMLEvent.START_ELEMENT) {

                switch (reader.getName().getLocalPart()) {

                    case "oppgave":
                        task = new Task();
                        break;

                    case "oppgaveeier":
                        task.owner = parseTaskOwner(reader);
                        break;

                    case "kontonummer":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            task.accountNumber = reader.getText();
                        }
                        break;

                    case "skattekontoEgnethet":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            task.taxAccountAptitude = reader.getText();
                        }
                        break;

                    case "innskudd":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            task.deposit = reader.getText();
                        }
                        break;

                    case "opptjenteRenter":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            task.earnedInterest = reader.getText();
                        }
                        break;

                    case "paaloepteRenter":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            task.accruedInterest = reader.getText();
                        }
                        break;

                    case "kontotype":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            task.accountType = reader.getText();
                        }
                        break;
                }

            }

            if (eventType == XMLEvent.END_ELEMENT) {
                // if </staff>
                if (reader.getName().getLocalPart().equals("oppgave")) {
                    task.delivery = delivery;
                    taskEmitter.send(task);
                    counter++;
                    //System.out.println("task = " + task);
                }
            }
        }

    }

    private void parseMessage(XMLStreamReader reader) throws XMLStreamException {
        int eventType;
        while(reader.hasNext()) {
            eventType = reader.next();
            if (eventType == XMLEvent.START_ELEMENT) {
                if (reader.getName().getLocalPart().equals("melding")) {
                    //TODO: add some logic here
                    break;
                }
            }
        }
        //check if we never found melding, if so we should throw an exception
        if(!reader.hasNext())
            throw new XMLStreamException("Failed to find the melding element, aborting");
    }

    private Delivery parseOwner(XMLStreamReader reader) throws XMLStreamException {
        Delivery delivery = new Delivery();
        DeliveryOwner owner = new DeliveryOwner();
        ContactInfo contact = new ContactInfo();

        int eventType;
        while (reader.hasNext()) {
            eventType = reader.next();
            if (eventType == XMLEvent.START_ELEMENT) {
                switch (reader.getName().getLocalPart()) {

                    case "oppgavegiver":
                        delivery.deliveryOwner = parseDeliveryOwner(reader);
                        break;

                    case "kildesystem":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            delivery.sourceSystem = reader.getText();
                        }
                        break;

                    case "inntektsaar":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            delivery.fiscalYear = reader.getText();
                        }
                        break;

                    case "oppgavegiversLeveranseReferanse":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            delivery.taskDeliveryReference = reader.getText();
                        }
                        break;

                    case "leveransetype":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            delivery.deliveryType = reader.getText();
                        }
                        break;

                    case "presentasjonsnavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            delivery.presentingName = reader.getText();
                        }
                        owner.contactInfo = contact;
                        delivery.deliveryOwner = owner;
                        return delivery;
                }
            }
        }
        return delivery;
    }

    private DeliveryOwner parseDeliveryOwner(XMLStreamReader reader) throws XMLStreamException {
        int eventType;
        DeliveryOwner owner = new DeliveryOwner();
        owner.contactInfo = new ContactInfo();
        while (reader.hasNext()) {
            eventType = reader.next();
            if (eventType == XMLEvent.START_ELEMENT) {

                switch (reader.getName().getLocalPart()) {

                    case "organisasjonsnummer":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.orgNumber = reader.getText();
                        }
                        break;

                    case "organisasjonsnavn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.orgName = reader.getText();
                        }
                        break;

                    case "navn":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.contactInfo.name = reader.getText();
                        }
                        break;

                    case "telefonnummer":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.contactInfo.phone = reader.getText();
                        }
                        break;

                    case "varselEpostadresse":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.contactInfo.email = reader.getText();
                        }
                        break;

                    case "varselSmsMobilnummer":
                        eventType = reader.next();
                        if (eventType == XMLEvent.CHARACTERS) {
                            owner.contactInfo.mobile = reader.getText();
                        }
                        break;

                }

            }

            if (eventType == XMLEvent.END_ELEMENT) {
                // if </staff>
                if (reader.getName().getLocalPart().equals("oppgavegiver")) {
                    return owner;
                }
            }
        }
        throw new XMLStreamException("Expected end tag oppgavegiver");
    }

}
