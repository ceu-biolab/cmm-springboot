package ceu.biolab.cmm.MSMS.service;

import ceu.biolab.cmm.MSMS.domain.Peak;
import ceu.biolab.cmm.MSMS.domain.ToleranceMode;

import java.util.*;

public class SpectrumScorer {
    public enum ScoreType {COSINE, MODIFIED_COSINE}

    private final ToleranceMode tolMode;
    private final double tolValue;
    private final double mzExp, intenExp;

    public SpectrumScorer(ToleranceMode tolMode, double tolValue, double mzExp, double intenExp) {
        this.tolMode = tolMode;
        this.tolValue = tolValue;
        this.mzExp = mzExp;
        this.intenExp = intenExp;
    }

    public double compute(ScoreType type, List<Peak> spec1, double precursor1, List<Peak> spec2, double precursor2) {
        switch (type) {
            case COSINE:
                return cosineScore(spec1, spec2).score;
            // case MODIFIED_COSINE:
            //   return modifiedCosine(spec1,  spec2, this.tolValue,this.tolMode==ToleranceMode.PPM);
            default:
                throw new IllegalArgumentException("Unknown score type");
        }
    }

    public  static class Score {
        private final double score;
        private final int matches;
        public Score(double score, int matches) {
            this.score   = score;
            this.matches = matches;
        }
        public double getScore()   { return score; }
        public int    getMatches() { return matches; }
        @Override
        public String toString() {
            return String.format("Score{score=%.4f, matches=%d}", score, matches);
        }
    }


        public Score cosineScore(List<Peak> spec1, List<Peak> spec2) {
            // 1) Sort both by m/z
            spec1.sort(Comparator.comparingDouble(Peak::getMz));
            spec2.sort(Comparator.comparingDouble(Peak::getMz));

            // 2) Precompute weighted intensities and norms
            int  n1 = spec1.size(), n2 = spec2.size();
            double[] w1 = new double[n1], w2 = new double[n2];
            double norm1 = 0, norm2 = 0;
            for (int i = 0; i < n1; i++) {
                Peak p = spec1.get(i);
                double wi = Math.pow(p.getMz(), mzExp) * Math.pow(p.getIntensity(), intenExp);
                w1[i] = wi;
                norm1 += wi * wi;
            }
            for (int j = 0; j < n2; j++) {
                Peak p = spec2.get(j);
                double wi = Math.pow(p.getMz(), mzExp) * Math.pow(p.getIntensity(), intenExp);
                w2[j] = wi;
                norm2 += wi * wi;
            }
            if (norm1 == 0 || norm2 == 0) {
                return new Score(0.0, 0);
            }

            // 3) Collect all candidate matches within tolerance
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < n1; i++) {
                double mz1 = spec1.get(i).getMz();
                for (int j = 0; j < n2; j++) {
                    double mz2 = spec2.get(j).getMz();
                    double delta = Math.abs(mz1 - mz2);
                    double effTol = tolMode.equals(ToleranceMode.PPM)
                            ?    ((mz1 + mz2) / 2.0) * tolValue / 1e6
                            : tolValue;
                    if (delta <= effTol) {
                        double score = w1[i] * w2[j];
                        matches.add(new Match(i, j, score));
                    }
                }
            }

            // 4) Greedily pick the best non-overlapping matches
            Collections.sort(matches, Comparator.comparingDouble(Match::getScore).reversed());
            boolean[] used1 = new boolean[n1];
            boolean[] used2 = new boolean[n2];
            double dot = 0;
            int    count = 0;
            for (Match m : matches) {
                if (!used1[m.i] && !used2[m.j]) {
                    dot       += m.score;
                    used1[m.i] = true;
                    used2[m.j] = true;
                    count++;
                }
            }

            // 5) Normalize
            double cosine = dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
            return new Score(cosine, count);
        }

    private static class Match {
        final int i, j;
        final double score;
        Match(int i, int j, double score) {
            this.i     = i;
            this.j     = j;
            this.score = score;
        }
        public double getScore() { return score; }
    }


}

