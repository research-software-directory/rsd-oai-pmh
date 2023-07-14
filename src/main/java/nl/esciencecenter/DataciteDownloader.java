package nl.esciencecenter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class DataciteDownloader {

	public static final URI RSD_RELEASES = URI.create("https://research-software-directory.org/api/v1/release_version?select=mention(doi)");
	public static final String DATACITE_OAI = "https://oai.datacite.org/oai/?verb=GetRecord&metadataPrefix=oai_datacite&identifier=doi:";
	public static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newDefaultFactory();
	public static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newDefaultFactory();
	public static final XMLEventFactory XML_EVENT_FACTORY = XMLEventFactory.newDefaultFactory();
	public static final String ZENODO_PREFIX = "10.5281/ZENODO.";
	public static final String ZENODO_PREFIX_QUOTED = Pattern.quote(ZENODO_PREFIX);


	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Start scraping OAI-PMH data from DataCite");
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(RSD_RELEASES)
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) throw new RuntimeException("RSD status is " + response.statusCode());

		JsonArray releasesJson = JsonParser.parseString(response.body()).getAsJsonArray();
		List<String> dois = new ArrayList<>();
		for (JsonElement jsonElement : releasesJson) {
			String doi = jsonElement.getAsJsonObject().getAsJsonObject("mention").getAsJsonPrimitive("doi").getAsString();
			dois.add(doi);
		}

		Collection<Callable<Void>> tasks = new ArrayList<>();
		for (String doi : dois) {
			Callable<Void> task = () -> {
				String fileName = encodeDoi(doi) + ".xml";
				Path path = Path.of(args[0], fileName);
				try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path));
					 InputStream inputStream = new BufferedInputStream(downloadDataciteOaiData(doi))) {

					XMLEventReader xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader(inputStream);
					XMLEventWriter xmlEventWriter = XML_OUTPUT_FACTORY.createXMLEventWriter(outputStream);
					boolean inResource = false;
					String responseDate = null;
					String datestamp = null;
					Optional<String> zenodoIdOptional = zenodoIdFromDoi(doi);
					while (xmlEventReader.hasNext()) {
						XMLEvent event = xmlEventReader.nextEvent();

						if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("responseDate")) {
							XMLEvent datestampEvent = xmlEventReader.nextEvent();
							responseDate = datestampEvent.asCharacters().getData();
						}

						if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("datestamp")) {
							XMLEvent datestampEvent = xmlEventReader.nextEvent();
							datestamp = datestampEvent.asCharacters().getData();
						}

						if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("resource")) {
							inResource = true;
							xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "resource"));
							continue;
						}
						if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("resource")) {
							inResource = false;
							if (zenodoIdOptional.isPresent()) {
								writeZenodoAboutSection(xmlEventWriter, zenodoIdOptional.get(), responseDate, datestamp);
							}
							xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "resource"));
							continue;
						}

						if (inResource) {
							xmlEventWriter.add(event);
						}

					}
					xmlEventWriter.flush();
				}
				return null;
			};
			tasks.add(task);
		}

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		try {
			List<Future<Void>> completedTasks = executorService.invokeAll(tasks);
			for (Future<Void> completedTask : completedTasks) {
				try {
					completedTask.get();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			executorService.shutdownNow();
		}
		System.out.println("Done scraping OAI-PMH data from DataCite");
	}

	public static InputStream downloadDataciteOaiData(String doi) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(URI.create(DATACITE_OAI + doi))
				.build();

		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) throw new RuntimeException("DataCite status is " + response.statusCode());

		return response.body();
	}

	public static void writeZenodoAboutSection(XMLEventWriter xmlEventWriter, String zenodoId, String responseDate, String datestamp) throws XMLStreamException {
		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "about"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "provenance"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createAttribute("xmlns", "http://www.openarchives.org/OAI/2.0/provenance"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createAttribute("xmlns", null,"xsi", "http://www.w3.org/2001/XMLSchema-instance"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createAttribute("xsi", null,"schemaLocation", "http://www.openarchives.org/OAI/2.0/provenance http://www.openarchives.org/OAI/2.0/provenance.xsd"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "originDescription"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createAttribute("harvestDate", responseDate));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "baseURL"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createCharacters("https://zenodo.org/"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "baseURL"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "identifier"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createCharacters("oai:zenodo.org/" + zenodoId));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "identifier"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "datestamp"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createCharacters(datestamp));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "datestamp"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "metadataNamespace"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createCharacters("http://www.openarchives.org/OAI/2.0/"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "metadataNamespace"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "repositoryId"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createCharacters("opendoar:2659"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "repositoryId"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "repositoryName"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createCharacters("ZENODO"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "repositoryName"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "originDescription"));

		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "provenance"));
		xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "about"));
	}

	public static String encodeDoi(String doi) {
		return URLEncoder.encode(doi, StandardCharsets.UTF_8);
	}

	public static Optional<String> zenodoIdFromDoi(String doi) {
		doi = doi.toUpperCase();
		if (!doi.startsWith(ZENODO_PREFIX)) {
			return Optional.empty();
		}

		String zenodoId = doi.replaceFirst(ZENODO_PREFIX_QUOTED, "");
		return Optional.of(zenodoId);
	}
}
