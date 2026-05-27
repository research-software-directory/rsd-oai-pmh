package nl.esciencecenter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.stream.Stream;

// unused, not sure why this was written, maybe it can be deleted
@Deprecated
public class Main {

	public static void main(String[] args) throws IOException, InterruptedException, XMLStreamException {
		try (Stream<OaiDataciteStruct> items = RsdPostgrestOaiSource.getDataciteItems()) {
			OaiDataciteXml.writeXml(items, new OutputStreamWriter(System.out));
		}
	}
}
