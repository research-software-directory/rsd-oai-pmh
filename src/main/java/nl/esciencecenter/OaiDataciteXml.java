package nl.esciencecenter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.stream.Stream;

public class OaiDataciteXml {

	public static void writeXml(Stream<OaiDataciteStruct> items, Writer writer) throws XMLStreamException, IOException {
		XMLStreamWriter xmlWriter = XMLOutputFactory.newDefaultFactory().createXMLStreamWriter(writer);
		xmlWriter.writeStartDocument();

		items.filter(Objects::nonNull)
				.forEach(item -> {
					try {
						xmlWriter.writeStartElement("identifier");
						xmlWriter.writeAttribute("identifierType", item.identifier.identifierType.toString());
						xmlWriter.writeCharacters(item.identifier.identifier);
						xmlWriter.writeEndElement();

						xmlWriter.writeStartElement("creators");
						for (OaiDataciteStruct.Creator creator : item.creator) {
							xmlWriter.writeStartElement("creator");
							xmlWriter.writeStartElement("creatorName");
							xmlWriter.writeCharacters(creator.creatorName);
							xmlWriter.writeEndElement();
							if (creator.nameIdentifier != null) {
								xmlWriter.writeStartElement("nameIdentifier");
								xmlWriter.writeAttribute("nameIdentifierScheme", creator.nameIdentifier.nameIdentifierScheme);
								if (creator.nameIdentifier.schemeUri != null)
									xmlWriter.writeAttribute("schemeURI", creator.nameIdentifier.schemeUri);
								xmlWriter.writeCharacters(creator.nameIdentifier.value);
								xmlWriter.writeEndElement();
							}
							for (String affiliation : creator.affiliation) {
								xmlWriter.writeStartElement("affiliation");
								xmlWriter.writeCharacters(affiliation);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}
						xmlWriter.writeEndElement();

						xmlWriter.writeStartElement("titles");
						for (OaiDataciteStruct.Title title : item.title) {
							xmlWriter.writeStartElement("title");
							if (title.titleType != null)
								xmlWriter.writeAttribute("titleType", title.titleType.toString());
							xmlWriter.writeCharacters(title.title);
							xmlWriter.writeEndElement();
						}
						xmlWriter.writeEndElement();

						xmlWriter.writeStartElement("publisher");
						xmlWriter.writeCharacters(item.publisher);
						xmlWriter.writeEndElement();

						xmlWriter.writeStartElement("publicationYear");
						xmlWriter.writeCharacters(item.publicationYear.toString());
						xmlWriter.writeEndElement();

						if (item.subject != null && !item.subject.isEmpty()) {
							xmlWriter.writeStartElement("subjects");
							for (OaiDataciteStruct.Subject subject : item.subject) {
								xmlWriter.writeStartElement("subject");
								if (subject.subjectScheme != null)
									xmlWriter.writeAttribute("subjectScheme", subject.subjectScheme);
								if (subject.schemeUri != null) xmlWriter.writeAttribute("schemeURI", subject.schemeUri);
								xmlWriter.writeCharacters(subject.value);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}

						xmlWriter.writeStartElement("contributors");
						for (OaiDataciteStruct.Contributor contributor : item.contributor) {
							xmlWriter.writeStartElement("contributor");
							xmlWriter.writeAttribute("contributorType", contributor.contributorType.toString());
							xmlWriter.writeStartElement("contributorName");
							xmlWriter.writeCharacters(contributor.contributorName);
							xmlWriter.writeEndElement();
							if (contributor.nameIdentifier != null) {
								xmlWriter.writeStartElement("nameIdentifier");
								xmlWriter.writeAttribute("nameIdentifierScheme", contributor.nameIdentifier.nameIdentifierScheme);
								if (contributor.nameIdentifier.schemeUri != null)
									xmlWriter.writeAttribute("schemeURI", contributor.nameIdentifier.schemeUri);
								xmlWriter.writeCharacters(contributor.nameIdentifier.value);
								xmlWriter.writeEndElement();
							}
							for (String affiliation : contributor.affiliation) {
								xmlWriter.writeStartElement("affiliation");
								xmlWriter.writeCharacters(affiliation);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}
						xmlWriter.writeEndElement();

						xmlWriter.writeStartElement("dates");
						for (OaiDataciteStruct.Date date : item.date) {
							xmlWriter.writeStartElement("date");
							xmlWriter.writeAttribute("dateType", date.dateType.toString());
							xmlWriter.writeCharacters(date.date.toString());
							xmlWriter.writeEndElement();
						}
						xmlWriter.writeEndElement();

						if (item.language != null) {
							xmlWriter.writeStartElement("language");
							xmlWriter.writeCharacters(item.language);
							xmlWriter.writeEndElement();
						}

						if (item.resourceType != null) {
							xmlWriter.writeStartElement("resourceType");
							xmlWriter.writeAttribute("resourceTypeGeneral", item.resourceType.resourceTypeGeneral.toString());
							xmlWriter.writeCharacters(item.resourceType.value);
							xmlWriter.writeEndElement();
						}

						if (item.alternateIdentifier != null && !item.alternateIdentifier.isEmpty()) {
							xmlWriter.writeStartElement("alternateIdentifiers");
							for (OaiDataciteStruct.AlternateIdentifier alternateIdentifier : item.alternateIdentifier) {
								xmlWriter.writeStartElement("alternateIdentifier");
								xmlWriter.writeAttribute("alternateIdentifierType", alternateIdentifier.alternateIdentifierType);
								xmlWriter.writeCharacters(alternateIdentifier.value);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}

						if (item.resourceType != null && !item.relatedIdentifier.isEmpty()) {
							xmlWriter.writeStartElement("relatedIdentifiers");
							for (OaiDataciteStruct.RelatedIdentifier relatedIdentifier : item.relatedIdentifier) {
								xmlWriter.writeStartElement("relatedIdentifier");
								xmlWriter.writeAttribute("relatedIdentifierType", relatedIdentifier.relatedIdentifierType.toString());
								xmlWriter.writeAttribute("relationType", relatedIdentifier.relationType.toString());
								if (relatedIdentifier.relatedMetadataScheme != null)
									xmlWriter.writeAttribute("relatedMetadataScheme", relatedIdentifier.relatedMetadataScheme);
								if (relatedIdentifier.schemeUri != null)
									xmlWriter.writeAttribute("schemeURI", relatedIdentifier.schemeUri);
								if (relatedIdentifier.schemeType != null)
									xmlWriter.writeAttribute("schemeType", relatedIdentifier.schemeType);
								xmlWriter.writeCharacters(relatedIdentifier.value);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}

						if (item.size != null && !item.size.isEmpty()) {
							xmlWriter.writeStartElement("sizes");
							for (String size : item.size) {
								xmlWriter.writeStartElement("size");
								xmlWriter.writeCharacters(size);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}

						if (item.version != null) {
							xmlWriter.writeStartElement("version");
							xmlWriter.writeCharacters(item.version);
							xmlWriter.writeEndElement();
						}

						if (item.rights != null && !item.rights.isEmpty()) {
							xmlWriter.writeStartElement("rightsList");
							for (OaiDataciteStruct.Rights rights : item.rights) {
								if (rights.value == null && rights.rightsUri == null) continue;
								if (rights.value == null) {
									xmlWriter.writeEmptyElement("rights");
									xmlWriter.writeAttribute("rightsURI", rights.rightsUri);
								} else if (rights.rightsUri == null) {
									xmlWriter.writeStartElement("rights");
									xmlWriter.writeCharacters(rights.value);
									xmlWriter.writeEndElement();
								} else {
									xmlWriter.writeStartElement("rights");
									xmlWriter.writeAttribute("rightsURI", rights.rightsUri);
									xmlWriter.writeCharacters(rights.value);
									xmlWriter.writeEndElement();
								}
							}
							xmlWriter.writeEndElement();
						}

						if (item.description != null && !item.description.isEmpty()) {
							xmlWriter.writeStartElement("descriptions");
							for (OaiDataciteStruct.Description description : item.description) {
								xmlWriter.writeStartElement("description");
								xmlWriter.writeAttribute("descriptionType", description.descriptionType.toString());
								xmlWriter.writeCharacters(description.value);
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}

						if (item.geoLocation != null && !item.geoLocation.isEmpty()) {
							xmlWriter.writeStartElement("geoLocations");
							for (OaiDataciteStruct.GeoLocation geoLocation : item.geoLocation) {
								xmlWriter.writeStartElement("geoLocation");
								if (geoLocation.geoLocationPoint != null) {
									xmlWriter.writeStartElement("geoLocationPoint");
									xmlWriter.writeCharacters(geoLocation.geoLocationPoint);
									xmlWriter.writeEndElement();
								}
								if (geoLocation.geoLocationBox != null) {
									xmlWriter.writeStartElement("geoLocationBox");
									xmlWriter.writeCharacters(geoLocation.geoLocationBox);
									xmlWriter.writeEndElement();
								}
								if (geoLocation.geoLocationPlace != null) {
									xmlWriter.writeStartElement("geoLocationPlace");
									xmlWriter.writeCharacters(geoLocation.geoLocationPlace);
									xmlWriter.writeEndElement();
								}
								xmlWriter.writeEndElement();
							}
							xmlWriter.writeEndElement();
						}
					} catch (XMLStreamException e) {
						throw new RuntimeException(e);
					}
				});
		xmlWriter.writeEndDocument();
		writer.flush();
	}
}
