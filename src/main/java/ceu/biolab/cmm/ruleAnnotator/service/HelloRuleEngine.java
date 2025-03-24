package ceu.biolab.cmm.ruleAnnotator.service;

import java.util.ArrayList;

import ceu.biolab.cmm.ruleAnnotator.model.HelloUnit;
import ceu.biolab.cmm.ruleAnnotator.model.Hello;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.springframework.stereotype.Service;

@Service
public class HelloRuleEngine {

    private HelloUnit helloUnit = new HelloUnit();

    public ArrayList<String> addHelloAndRun(String hello) {
        RuleUnitInstance<HelloUnit> ruleUnitInstance = RuleUnitProvider.get().createRuleUnitInstance(helloUnit);

        helloUnit.getHellos().add(new Hello(hello));

        ruleUnitInstance.fire();

        ArrayList<String> results = new ArrayList<>();
        helloUnit.getControlSet().forEach(results::add);

        return results;
    }
}
