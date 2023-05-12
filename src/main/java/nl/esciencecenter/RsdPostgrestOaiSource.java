package nl.esciencecenter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class RsdPostgrestOaiSource {

	public static Stream<OaiDataciteStruct> getDataciteItems() throws IOException, InterruptedException {
		JsonReader jsonReader = new JsonReader(new InputStreamReader(downloadSoftware()));
		jsonReader.beginArray();
		Stream<OaiDataciteStruct> result = Stream.iterate(null,
				t -> {
					try {
						boolean hasNext = jsonReader.hasNext();
						if (!hasNext) {
							jsonReader.endArray();
							jsonReader.close();
						}
						return hasNext;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				},
				t -> {
					try {
						return readSoftware(jsonReader);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		result = result.onClose(() -> {
			try {
				jsonReader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		// skips the null seed
		result = result.skip(1);
		return result;
	}

	private static InputStream downloadSoftware() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		String selectList = "concept_doi,brand_name,short_statement,contributor(family_names,given_names,affiliation,role,is_contact_person)";
		HttpRequest request = HttpRequest.newBuilder(URI.create("https://research-software-directory.org/api/v1/software?select=" + selectList))
				.build();

		HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() != 200)
			throw new RuntimeException("Unexpected response with status code: " + response.statusCode());

		return response.body();
	}

	private static String readStringOrNull(JsonReader jsonReader) throws IOException {
		return switch (jsonReader.peek()) {
			case NULL -> {jsonReader.nextNull(); yield null;}
			case STRING -> jsonReader.nextString();
			default -> throw new RuntimeException("Tried to read a String or null but got " + jsonReader.peek().toString() + " at " + jsonReader.getPath());
		};
	}

	private static OaiDataciteStruct readSoftware(JsonReader jsonReader) throws IOException {
		OaiDataciteStruct software = new OaiDataciteStruct();
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			String name = jsonReader.nextName();
			switch (name) {
				case "concept_doi" -> software.identifier = readConceptDoi(jsonReader);
				case "brand_name" -> software.title = List.of(readTitle(jsonReader));
				case "short_statement" -> software.description = List.of(readShortStatement(jsonReader));
				case "contributor" -> software.creator = readContributors(jsonReader);
				default -> jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
		return software;
	}

	private static OaiDataciteStruct.Identifier readConceptDoi(JsonReader jsonReader) throws IOException {
		if (jsonReader.peek() == JsonToken.NULL) {
			jsonReader.nextNull();
			return null;
		}

		OaiDataciteStruct.Identifier result = new OaiDataciteStruct.Identifier();
		result.identifier = jsonReader.nextString();
		result.identifierType = OaiDataciteStruct.Identifier.IdentifierType.DOI;
		return result;
	}

	private static OaiDataciteStruct.Title readTitle(JsonReader jsonReader) throws IOException {
		OaiDataciteStruct.Title result = new OaiDataciteStruct.Title();
		result.title = jsonReader.nextString();
		return result;
	}

	private static OaiDataciteStruct.Description readShortStatement(JsonReader jsonReader) throws IOException {
		OaiDataciteStruct.Description result = new OaiDataciteStruct.Description();
		result.value = jsonReader.nextString();
		result.descriptionType = OaiDataciteStruct.Description.DescriptionType.Abstract;
		return result;
	}

	private static Collection<OaiDataciteStruct.Creator> readContributors(JsonReader jsonReader) throws IOException {
		Collection<OaiDataciteStruct.Creator> result = new ArrayList<>();
		jsonReader.beginArray();
		while (jsonReader.hasNext()) {
			jsonReader.beginObject();
			OaiDataciteStruct.Creator creator = new OaiDataciteStruct.Creator();
			String familyNames = null;
			String givenNames = null;
			String affiliation = null;
			while (jsonReader.hasNext()) {
				String name = jsonReader.nextName();
				switch (name) {
					case "family_names" -> familyNames = jsonReader.nextString();
					case "given_names" -> givenNames = jsonReader.nextString();
					case "affiliation" -> affiliation = readStringOrNull(jsonReader);
					case "role" -> jsonReader.skipValue();
					case "is_contact_person" -> jsonReader.skipValue();
					default -> jsonReader.skipValue();
				}
			}
			creator.creatorName = familyNames + ", " + givenNames;
			creator.affiliation = affiliation == null ? Collections.emptyList() : List.of(affiliation);
			jsonReader.endObject();
			result.add(creator);
		}
		jsonReader.endArray();
		return result;
	}

}
