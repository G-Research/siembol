package uk.co.gresearch.siembol.spark;

import uk.co.gresearch.siembol.alerts.common.AlertingEngine;
import uk.co.gresearch.siembol.alerts.compiler.AlertingRulesCompiler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AlertingSparkEngine implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ALERTING_RULE_COMPILATION_ERROR = "Exception during compiling alerting rules";
    private transient AlertingEngine alertingEngine;
    private String rules;

    public AlertingSparkResult eval(String event, int maxResult) {
        return new AlertingSparkResult(alertingEngine.evaluate(event), maxResult);
    }

    public AlertingSparkEngine(String rules) throws Exception {
        this.rules = rules;
        alertingEngine = AlertingRulesCompiler
                .createAlertingRulesCompiler()
                .compile(rules)
                .getAttributes()
                .getEngine();
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        os.writeUTF(rules);
    }

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        rules = is.readUTF();
        try {
            alertingEngine = AlertingRulesCompiler
                    .createAlertingRulesCompiler()
                    .compile(rules)
                    .getAttributes()
                    .getEngine();
        } catch (Exception e) {
            throw new IOException(ALERTING_RULE_COMPILATION_ERROR);
        }
    }
}
