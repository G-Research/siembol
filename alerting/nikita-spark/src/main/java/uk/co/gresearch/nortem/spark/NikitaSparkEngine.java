package uk.co.gresearch.nortem.spark;

import uk.co.gresearch.nortem.nikita.common.NikitaEngine;
import uk.co.gresearch.nortem.nikita.compiler.NikitaRulesCompiler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NikitaSparkEngine implements Serializable {
    private transient NikitaEngine nikitaEngine;
    private String rules;

    public NikitaSparkResult eval(String event, int maxResult) {
        return new NikitaSparkResult(nikitaEngine.evaluate(event), maxResult);
    }

    public NikitaSparkEngine(String rules) throws Exception {
        this.rules = rules;
        nikitaEngine = NikitaRulesCompiler
                .createNikitaRulesCompiler()
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
            nikitaEngine = NikitaRulesCompiler
                    .createNikitaRulesCompiler()
                    .compile(rules)
                    .getAttributes()
                    .getEngine();
        } catch (Exception e) {
            throw new IOException();
        }
    }
}
