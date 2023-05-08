import nl.esciencecenter.OaiDataciteStruct;
import nl.esciencecenter.OaiDataciteXml;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

public class OaiDataciteXmlTest {

	@Test
	void givenOneValidOaiDataciteItem_whenWritingXml_noExceptionsThrown() {
		OaiDataciteStruct item = new OaiDataciteStruct();

		item.identifier = new OaiDataciteStruct.Identifier();
		item.identifier.identifier = "10.5281/zenodo.6379973";
		item.identifier.identifierType = OaiDataciteStruct.Identifier.IdentifierType.DOI;

		OaiDataciteStruct.Creator creator1 = new OaiDataciteStruct.Creator();
		creator1.creatorName = "Family, First";
		creator1.nameIdentifier = new OaiDataciteStruct.Creator.NameIdentifier();
		creator1.nameIdentifier.value = "1422 4586 3573 0476";
		creator1.nameIdentifier.nameIdentifierScheme = "ISNI";
		creator1.nameIdentifier.schemeUri = "http://www.isni.org";
		creator1.affiliation = List.of("NLeSC", "NWO");
		item.creator = List.of(creator1);

		OaiDataciteStruct.Title title1 = new OaiDataciteStruct.Title();
		title1.title = "RSD SaaS";
		item.title = List.of(title1);

		item.publisher = "World Data Center for Climate (WDCC)";

		item.publicationYear = Year.of(2023);

		OaiDataciteStruct.Subject subject1 = new OaiDataciteStruct.Subject();
		subject1.value = "551 Geology, hydrology, meteorology";
		subject1.subjectScheme = "DDC";
		subject1.schemeUri = "http://dewey.info/";
		item.subject = List.of(subject1);

		OaiDataciteStruct.Contributor contributor1 = new OaiDataciteStruct.Contributor();
		contributor1.contributorType = OaiDataciteStruct.Contributor.ContributorType.Funder;
		contributor1.contributorName = "European Commission";
		contributor1.nameIdentifier = new OaiDataciteStruct.Contributor.NameIdentifier();
		contributor1.nameIdentifier.value = "info:eu-repo/grantAgreement/EC/FP7/282896";
		contributor1.nameIdentifier.nameIdentifierScheme = "info";
		contributor1.nameIdentifier.schemeUri = "http://www.crossref.org/fundref/";
		contributor1.affiliation = List.of("Affiliation 1", "Affiliation 2");
		item.contributor = List.of(contributor1);

		OaiDataciteStruct.Date date1 = new OaiDataciteStruct.Date();
		date1.date = LocalDate.of(2023, 5, 9);
		date1.dateType = OaiDataciteStruct.Date.DateType.Accepted;
		item.date = List.of(date1);

		item.language = "nl";

		item.resourceType = new OaiDataciteStruct.ResourceType();
		item.resourceType.value = "Animation";
		item.resourceType.resourceTypeGeneral = OaiDataciteStruct.ResourceType.ResourceTypeGeneral.Image;

		OaiDataciteStruct.AlternateIdentifier alternateIdentifier1 = new OaiDataciteStruct.AlternateIdentifier();
		alternateIdentifier1.value = "937-0-1234-56789-X";
		alternateIdentifier1.alternateIdentifierType = "ISBN";
		item.alternateIdentifier = List.of(alternateIdentifier1);

		OaiDataciteStruct.RelatedIdentifier relatedIdentifier1 = new OaiDataciteStruct.RelatedIdentifier();
		relatedIdentifier1.value = "10.1234/bar";
		relatedIdentifier1.relatedIdentifierType = OaiDataciteStruct.RelatedIdentifier.RelatedIdentifierType.DOI;
		relatedIdentifier1.relationType = OaiDataciteStruct.RelatedIdentifier.RelationType.IsCitedBy;
		item.relatedIdentifier = List.of(relatedIdentifier1);

		item.size = List.of("15 pages", "6 MB");

		item.format = List.of("PDF", "application/pdf");

		item.version = "1.0";

		OaiDataciteStruct.Rights rights1 = new OaiDataciteStruct.Rights();
		rights1.rightsUri = "info:eu-repo/semantics/openAccess";
		OaiDataciteStruct.Rights rights2 = new OaiDataciteStruct.Rights();
		rights2.rightsUri = "http://creativecommons.org/licenses/by/4.0/";
		rights2.value = "Creative Commons Attribution 4.0 International";
		item.rights = List.of(rights1, rights2);

		Writer memoryWriter = new CharArrayWriter();
		Stream<OaiDataciteStruct> items = Stream.of(item);

		OaiDataciteStruct.Description description1 = new OaiDataciteStruct.Description();
		description1.value = "This is an abstract";
		description1.descriptionType = OaiDataciteStruct.Description.DescriptionType.Abstract;
		OaiDataciteStruct.Description description2 = new OaiDataciteStruct.Description();
		description2.value = "This is e.g. a note.";
		description2.descriptionType = OaiDataciteStruct.Description.DescriptionType.Other;
		item.description = List.of(description1, description2);

		OaiDataciteStruct.GeoLocation geoLocation1 = new OaiDataciteStruct.GeoLocation();
		geoLocation1.geoLocationPoint = "31.233 -67.302";
		geoLocation1.geoLocationBox = "41.090 -71.032 42.893 -68.211";
		geoLocation1.geoLocationPlace = "Atlantic Ocean";
		item.geoLocation = List.of(geoLocation1);


		Assertions.assertDoesNotThrow(() -> OaiDataciteXml.writeXml(items, memoryWriter));
		System.out.println(memoryWriter);
	}
}
