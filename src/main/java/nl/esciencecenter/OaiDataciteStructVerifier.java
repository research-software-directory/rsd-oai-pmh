package nl.esciencecenter;

public class OaiDataciteStructVerifier {

	// TODO: all fields
	public static boolean isValid(OaiDataciteStruct item) {
		if (item.identifier == null) return false;
		if (item.identifier.identifier == null) return false;
		if (item.identifier.identifierType == null) return false;

		return true;
	}
}
