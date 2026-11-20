package ceu.biolab.cmm.compoundDetails.service;

import ceu.biolab.cmm.compoundDetails.dto.*;
import ceu.biolab.cmm.compoundDetails.repository.CompoundDetailsRepository;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CompoundDetailsService {
    private final CompoundService compounds;
    private final CompoundDetailsRepository repository;

    public CompoundDetailsService(CompoundService compounds, CompoundDetailsRepository repository) {
        this.compounds = compounds;
        this.repository = repository;
    }

    public CompoundDetailsResponse compound(int id) {
        positive(id);
        var compound = compounds.getCompoundById(id);
        var metadata = repository.findCompoundMetadata(id).orElseThrow(() -> missing("Compound", id));
        return new CompoundDetailsResponse(compound, metadata.created(), metadata.lastUpdated(),
                repository.compoundReactions(id), repository.compoundClassyfire(id), repository.compoundOntology(id),
                repository.compoundReferences(id), repository.compoundOrganisms(id));
    }

    public ClassyfireNodeResponse classyfire(String id) {
        String canonical = canonicalNodeId(id);
        return repository.findClassyfire(canonical).orElseThrow(() -> missing("ClassyFire node", canonical));
    }

    public OntologyTermResponse ontology(long id) {
        positive(id);
        return repository.findOntology(id).orElseThrow(() -> missing("Ontology term", id));
    }

    public ReferenceResponse reference(long id) {
        positive(id);
        var reference = repository.findReference(id).orElseThrow(() -> missing("Reference", id));
        return new ReferenceResponse(reference.referenceId(), reference.referenceText(), reference.doi(), reference.link(),
                reference.created(), reference.lastUpdated(), repository.referenceCompounds(id), repository.referenceOrganisms(id));
    }

    public OrganismResponse organism(long id) {
        positive(id);
        var organism = repository.findOrganism(id).orElseThrow(() -> missing("Organism", id));
        return new OrganismResponse(organism.organismId(), organism.organismName(), organism.parentId(), organism.organismLevel(),
                organism.created(), organism.lastUpdated(), repository.organismHierarchy(id), repository.organismCompounds(id),
                repository.organismReferences(id));
    }

    public static String canonicalNodeId(String id) {
        if (id == null || !id.matches("(?i)(?:CHEMONTID:)?[0-9]{7}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ClassyFire ID must be seven digits, optionally prefixed with CHEMONTID:");
        }
        return id.length() == 7 ? "CHEMONTID:" + id : id.toUpperCase(Locale.ROOT);
    }

    private void positive(long id) {
        if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID must be greater than zero");
    }
    private ResponseStatusException missing(String entity, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, entity + " not found: " + id);
    }
}
