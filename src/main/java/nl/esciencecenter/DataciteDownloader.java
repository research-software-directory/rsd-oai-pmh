package nl.esciencecenter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DataciteDownloader {

	public static final URI RSD_RELEASES = URI.create("https://research-software-directory.org/api/v1/release_version?select=mention(doi)");
	public static final String DATACITE_OAI = "https://oai.datacite.org/oai/?verb=GetRecord&metadataPrefix=oai_datacite&identifier=doi:";
	public static final DocumentBuilder DOCUMENT_BUILDER;
	public static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newDefaultFactory();
	public static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newDefaultFactory();
	public static final XMLEventFactory XML_EVENT_FACTORY = XMLEventFactory.newDefaultFactory();

	static {
		try {
			DOCUMENT_BUILDER = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

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
					while (xmlEventReader.hasNext()) {
						XMLEvent event = xmlEventReader.nextEvent();

						if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("resource")) {
							inResource = true;
							xmlEventWriter.add(XML_EVENT_FACTORY.createStartElement("", null, "resource"));
							continue;
						}
						if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("resource")) {
							inResource = false;
							xmlEventWriter.add(XML_EVENT_FACTORY.createEndElement("", null, "resource"));
							continue;
						}

						if (inResource) {
							xmlEventWriter.add(event);
						}

					}
					xmlEventWriter.flush();


					// inputStream.transferTo(outputStream);
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

	public static String encodeDoi(String doi) {
		return URLEncoder.encode(doi, StandardCharsets.UTF_8);
	}
}
