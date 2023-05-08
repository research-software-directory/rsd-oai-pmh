package nl.esciencecenter;

import java.time.Year;
import java.time.temporal.Temporal;
import java.util.Collection;

public class OaiDataciteStruct {

	public Identifier identifier;
	public Collection<Creator> creator;
	public Collection<Title> title;
	public String publisher;
	public Year publicationYear;
	public Collection<Subject> subject;
	public Collection<Contributor> contributor;
	public Collection<Date> date;
	public String language;
	public ResourceType resourceType;
	public Collection<AlternateIdentifier> alternateIdentifier;
	public Collection<RelatedIdentifier> relatedIdentifier;
	public Collection<String> size;
	public Collection<String> format;
	public String version;
	public Collection<Rights> rights;
	public Collection<Description> description;
	public Collection<GeoLocation> geoLocation;

	public static class Identifier {
		public String identifier;
		public IdentifierType identifierType;

		public enum IdentifierType {
			ARK,
			DOI,
			Handle,
			PURL,
			URN,
			URL
		}
	}

	public static class Creator {
		public String creatorName;
		public NameIdentifier nameIdentifier;
		public Collection<String> affiliation;

		public static class NameIdentifier {
			public String value;
			public String nameIdentifierScheme;
			public String schemeUri;
		}

	}

	public static class Title {
		public String title;
		public TitleType titleType;

		public enum TitleType {
			AlternativeTitle,
			Subtitle,
			TranslatedTitle
		}
	}

	public static class Subject {
		public String value;
		public String subjectScheme;
		public String schemeUri;
	}

	public static class Contributor {
		public ContributorType contributorType;
		public String contributorName;
		public NameIdentifier nameIdentifier;
		public Collection<String> affiliation;

		public enum ContributorType {
			ContactPerson,
			DataCollector,
			DataCurator,
			DataManager,
			Distributor,
			Editor,
			Funder,
			HostingInstitution,
			Producer,
			ProjectLeader,
			ProjectManager,
			ProjectMember,
			RegistrationAgency,
			RegistrationAuthority,
			RelatedPerson,
			Researcher,
			ResearchGroup,
			RightsHolder,
			Sponsor,
			Supervisor,
			WorkPackageLeader,
			Other
		}

		public static class NameIdentifier {
			public String value;
			public String nameIdentifierScheme;
			public String schemeUri;
		}
	}

	public static class Date {
		public Temporal date;
		public DateType dateType;

		public enum DateType {
			Accepted,
			Available,
			Copyrighted,
			Collected,
			Created,
			Issued,
			Submitted,
			Updated,
			Valid
		}
	}

	public static class ResourceType {
		public String value;
		public ResourceTypeGeneral resourceTypeGeneral;

		public enum ResourceTypeGeneral {
			Audiovisual,
			Collection,
			Dataset,
			Event,
			Image,
			InteractiveResource,
			Model,
			PhysicalObject,
			Service,
			Software,
			Sound,
			Text,
			Workflow,
			Other
		}
	}

	public static class AlternateIdentifier {
		public String value;
		public String alternateIdentifierType;
	}

	public static class RelatedIdentifier {
		public String value;
		public RelatedIdentifierType relatedIdentifierType;
		public RelationType relationType;
		public String relatedMetadataScheme;
		public String schemeUri;
		public String schemeType;

		public enum RelatedIdentifierType {
			ARK,
			arXiv,
			bibcode,
			DOI,
			EAN13,
			EISSN,
			Handle,
			ISBN,
			ISSN,
			ISTC,
			LISSN,
			LSID,
			PMID,
			PURL,
			UPC,
			URL,
			URN
		}

		public enum RelationType {
			IsCitedBy,
			Cites,
			IsSupplementTo,
			IsSupplementedBy,
			IsContinuedBy,
			Continues,
			HasMetadata,
			IsMetadataFor,
			IsNewVersionOf,
			IsPreviousVersionOf,
			IsPartOf,
			HasPart,
			IsReferencedBy,
			References,
			IsDocumentedBy,
			Documents,
			isCompiledBy,
			Compiles,
			IsVariantFormOf,
			IsOriginalFormOf,
			IsIdenticalTo,
			IsReviewedBy,
			Reviews,
			IsDerivedFrom,
			IsSourceOf
		}
	}

	public static class Rights {
		public String value;
		public String rightsUri;
	}

	public static class Description {
		public String value;
		public DescriptionType descriptionType;

		public enum DescriptionType {
			Abstract,
			Methods,
			SeriesInformation,
			TableOfContents,
			Other
		}
	}

	public static class GeoLocation {
		public String geoLocationPoint;
		public String geoLocationBox;
		public String geoLocationPlace;
	}
}
