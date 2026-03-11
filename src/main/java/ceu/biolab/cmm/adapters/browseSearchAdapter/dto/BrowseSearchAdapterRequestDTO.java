package ceu.biolab.cmm.adapters.browseSearchAdapter.dto;

import lombok.Data;

import ceu.biolab.cmm.adapters.shared.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
@Data
public class BrowseSearchAdapterRequestDTO {
	
    private String name = "";
	private boolean exactName = false;
	private String formula = "";
    
    @JsonDeserialize(contentUsing = ceu.biolab.cmm.adapters.shared.deserializer.LegacyDatabaseDeserializer.class)
    private List<LegacyDatabase> databases = java.util.Arrays.asList(LegacyDatabase.ALL_EXCEPT_MINE);
	
    @JsonDeserialize(using = ceu.biolab.cmm.adapters.shared.deserializer.LegacyMetaboliteTypeDeserializer.class)
    private LegacyMetaboliteType metabolitesType = LegacyMetaboliteType.ALL_EXCEPT_PEPTIDES;

	public BrowseSearchAdapterRequestDTO() {
	}

}
