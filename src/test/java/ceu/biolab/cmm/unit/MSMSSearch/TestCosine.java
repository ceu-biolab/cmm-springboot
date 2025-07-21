package ceu.biolab.cmm.unit.MSMSSearch;

import ceu.biolab.cmm.MSMS.domain.Spectrum;
import ceu.biolab.cmm.MSMS.service.SpectrumScorer;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;

import java.util.ArrayList;
import java.util.List;

public class TestCosine {

    public static void main(String[] args) {
        // 1) Espectro A: picos en 100 (int=1) y 200 (int=2)
        Spectrum specA = new Spectrum(new ArrayList<>(List.of(
                new MSPeak(100.0, 1.0),
                new MSPeak(200.0, 2.0)
        )
        ));
        // 2) Espectro B idéntico (debería dar cosine = 1.0)
        Spectrum specB1 = new Spectrum(new ArrayList<>(List.of(
                new MSPeak(100.0, 1.0),
                new MSPeak(200.0, 2.0)
        )));
        // 3) Espectro B con un pico “fuera de tolerancia” (200→205)
        Spectrum specB2 = new Spectrum(new ArrayList<>(List.of(
                new MSPeak(100.0, 1.0),
                new MSPeak(205.0, 2.0)
        )));

       SpectrumScorer instancia = new SpectrumScorer(MzToleranceMode.MDA, 0.1); // donde tengas tu cosineScore()

        // Primer test: coseno perfecto
        double score1 = instancia.cosineScore(specA, specB1);
        System.out.printf("Test1 (idénticos): %.4f  ← debería ser 1.0%n", score1);

        // Segundo test: segundo pico no coincide (tolerancia muy pequeña)
        double score2 = instancia.cosineScore(specA, specB2);
        // Solo emparejará el pico en 100; A=(1,2) → B=(1,0) → dot=1, normA=√5, normB=1 → 1/√5≈0.447
        System.out.printf("Test2 (un pico fuera): %.4f  ← debería ser ~0.4472%n", score2);

        // Tercer test: con tolerancia más amplia (205 entra en rango de 200±5)
        double score3 = instancia.cosineScore(specA, specB2);
        // Ahora coseno vuelve a 1.0 porque ambos picos coinciden dentro de ±5 Da
        System.out.printf("Test3 (toler=5 Da): %.4f  ← debería ser 1.0%n", score3);
    }
}
