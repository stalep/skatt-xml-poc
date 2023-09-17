# PoC for prosessering av innskudd, utlån og renter

Tanken bak PoCen er å separere prosessering/validering av XML og videre behandling av dataene i forskjellige applikasjoner.
En service prosesserer og validerer XML og så sender dataene ut på en meldingsbuss (AMQP) som så blir behandlet av en annen service.

Tre moduler:
- -api inneholder dataobjektene som er delt mellom -consumer og -parser
- -consumer er tenkt service for videre prosessering av dataobjektene
- -parser prosesserer XML og sender dataene vider på en meldingsbuss.

XML prosesseringen er ikke komplett, men mer ment som et grunnlag for videre arbeid.
Det er en test som verifiserer prosesseringen samt sjekker at korrekt antall meldinger er sendt til meldingsbussen.
Filen `crs-xml-parser/src/test/resources/data/saldorente_v3_eksempel_normal.xml` brukes i testen.
Den er også lokalt brukt som basis for noen større filer vi har testet med lokalt, vi har testet filer opp mot 1.9gb og vi kan kjøre det på 512mb heap (dette er som sagt ikke en fullstendig prosessering og mer testing må gjøres).


## For å starte applikasjonene i dev mode:

I den første terminalen, kjør:

```bash
> mvn -f crs-xml-parser quarkus:dev
```

I den andre terminalen, kjør:

```bash
> mvn -f crs-xml-consumer quarkus:dev
```  

Så kan man åpne en nettleser til `http://localhost:8080/files.html` for å laste opp filer for prosessering. 
Merk at disse filene bare blir lagret i en temporær mappe og blir automatisk slettet. Det er bare for mellomlagring i denne PoCen.

## For å starte applikasjonene i prod/jvm med docker/podman:

```bash
> mvn -f crs-xml-parser package
> mvn -f crs-xml-consumer package
```
Siden vi kjører i _prod_ modus so må vi også starte en AMQP 1.0 broker.
[docker-compose.yml](docker-compose.yml) filen starter brokeren og applikasjonene.

Start the broker and the applications using:

```bash
> docker compose up --build
```
Så kan man åpne en nettleser til `http://localhost:8080/files.html` for å laste opp filer for prosessering. 


## For å kjøre separate proesesser 

Vi må starte opp brokeren først slik:
```bash
> docker run -it --rm -p 8161:8161 -p 61616:61616 -p 5672:5672 -e AMQ_USER=quarkus -e AMQ_PASSWORD=quarkus quay.io/artemiscloud/activemq-artemis-broker:0.1.4
```

I den første terminalen, kjør:

```bash
> cd crs-xml-parser; mvn clean package; cd target;
> java -jar quarkus-app/quarkus-run.jar
```

I den andre terminalen, kjør:

```bash
> cd crs-xml-consumer; mvn clean package; cd target;
> java -jar quarkus-app/quarkus-run.jar
```  

Så kan man åpne en nettleser til `http://localhost:8080/files.html` for å laste opp filer for prosessering. 

