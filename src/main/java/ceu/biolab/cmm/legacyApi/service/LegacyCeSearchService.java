package ceu.biolab.cmm.legacyApi.service;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.mapper.*;
import ceu.biolab.cmm.CEMSSearch.dto.*;
import ceu.biolab.cmm.CEMSSearch.service.*;
import ceu.biolab.cmm.CEMSMarkers.service.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LegacyCeSearchService {
    private final CemsSearchService effmob;
    private final CemsRmtSearchService rmt;
    private final Cems1MarkerService mt1;
    private final Cems2MarkerService mt2;
    private final CemsRmt1MarkerService rmt1;
    private final CemsRmt2MarkerService rmt2;
    private final LegacyCeRequestMapper requests;
    private final LegacyCeResponseMapper responses;
    public LegacyCeSearchService(CemsSearchService effmob, CemsRmtSearchService rmt, Cems1MarkerService mt1,
            Cems2MarkerService mt2, CemsRmt1MarkerService rmt1, CemsRmt2MarkerService rmt2,
            LegacyCeRequestMapper requests, LegacyCeResponseMapper responses) {
        this.effmob = effmob; this.rmt = rmt; this.mt1 = mt1; this.mt2 = mt2;
        this.rmt1 = rmt1; this.rmt2 = rmt2; this.requests = requests; this.responses = responses;
    }
    public LegacyResults.CeResults search(String route, LegacyCeRequest input) {
        CemsSearchResponseDTO result = switch (route) {
            case "effmob" -> effmob.search(requests.effmob(input));
            case "exprmt" -> rmt.search(requests.rmt(input));
            case "mt1marker" -> mt1.search(requests.mt1(input));
            case "mt2marker" -> mt2.search(requests.mt2(input));
            case "rmt1marker" -> rmt1.search(requests.rmt1(input));
            case "rmt2marker" -> rmt2.search(requests.rmt2(input));
            default -> throw new IllegalStateException("Unregistered CE route: " + route);
        };
        return responses.map(result, input, route);
    }
}
