import nl.esciencecenter.DataciteDownloader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DataciteDownloaderTest {

	@Test
	public void inspectWriteZenodoAboutSection() throws XMLStreamException {
		XMLEventWriter xmlEventWriter = XMLOutputFactory.newDefaultFactory().createXMLEventWriter(System.out);

		DataciteDownloader.writeZenodoAboutSection(
				xmlEventWriter,
				"12345",
				ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
				ZonedDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_INSTANT)
		);

		xmlEventWriter.flush();
	}

	@Test
	public void zenodoIdFromDoiTest() {
		String zenodoDoi = "10.5281/zenodo.1043306";
		Optional<String> zenodoIdOptional = DataciteDownloader.zenodoIdFromDoi(zenodoDoi);

		Assertions.assertTrue(zenodoIdOptional.isPresent());
		Assertions.assertEquals("1043306", zenodoIdOptional.get());

		String nonZenodoDOi = "abc";
		Optional<String> emptyZenodoIdOptional = DataciteDownloader.zenodoIdFromDoi(nonZenodoDOi);
		Assertions.assertTrue(emptyZenodoIdOptional.isEmpty());
	}
}
