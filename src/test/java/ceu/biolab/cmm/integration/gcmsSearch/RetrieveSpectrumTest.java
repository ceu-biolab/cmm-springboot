package ceu.biolab.cmm.integration.gcmsSearch;

import ceu.biolab.cmm.shared.domain.msFeature.Peak;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RetrieveSpectrumTest {
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public RetrieveSpectrumTest(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void testInsertSpectraDBId43(){
        String sql = "SELECT p.mz, p.intensity FROM compound_identifiers as i " +
                "INNER JOIN compounds as c ON i.compound_id = c.compound_id " +
                "INNER JOIN gcms_spectrum as s ON c.compound_id = s.compound_id " +
                "INNER JOIN gcms_peaks as p ON s.gcms_spectrum_id = p.gcms_spectrum_id " +
                "WHERE i.inchi = :testinchi";

        int spectrumId = 43; //The original list (txt) id is 44
        String testinchi = "InChI=1S/C23H48/c1-3-5-7-9-11-13-15-17-19-21-23-22-20-18-16-14-12-10-8-6-4-2/h3-23H2,1-2H3";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("testinchi", testinchi);
        Spectrum spectrumDB = jdbcTemplate.query(sql, param, spectrumPeaksExtractor(spectrumId));

        double expect_mz[] = {57.0699, 71.0855, 85.1012, 99.1168, 113.1325, 55.0542, 69.0699, 83.0855, 97.1012,
                127.1481, 70.0777, 56.0621, 141.1638, 84.0934, 111.1168, 155.1794, 169.1951, 98.1090, 72.0889, 82.0777,
                324.3751, 86.1046, 112.1247, 58.0733, 183.2107, 126.1403, 125.1325, 140.1560, 197.2264, 96.0934,
                154.1716, 67.0542, 211.2420, 168.1873, 239.2733, 225.2577, 68.0621, 100.1202, 110.1090, 182.2029, 253.2890,
                196.2186, 267.3046, 114.1359, 139.1481, 81.0699, 210.2342, 224.2499, 124.1247, 54.0464, 252.2812, 128.1515,
                281.3203, 238.2655, 142.1672, 138.1403, 266.2968, 325.3785, 53.0386, 153.1638, 95.0855, 156.1828, 170.1985,
                295.3359, 152.1560, 167.1794, 184.2141, 198.2298, 280.3125, 212.2454, 240.2812, 79.0542};
        double expect_intensity[] = {9999, 8621.6167521, 7233.8105466, 2523.2216526, 1620.5829255, 1585.9043937,
                1378.9270935, 1160.6029281, 1116.8773011, 1101.2648625, 1003.4626437, 967.43134719, 767.43814851,
                601.33746024, 580.81771242, 545.87910663, 444.23807175, 434.07228843, 379.0950867, 360.23847255,
                355.44885156, 352.80681579, 348.76032048, 335.72182446, 309.31126578, 308.30886603, 304.05129183,
                251.3208654, 250.0009974, 247.22257527, 227.65563216, 221.58903888, 195.18817923, 181.31936625, 179.33596461,
                176.21957628, 174.16908135, 168.57344097, 161.05319307, 153.44755371, 150.60403809, 146.90610792, 138.31696692,
                126.74282445, 126.34946379, 123.38336043, 117.59353947, 110.34946395, 102.94660431, 101.69902908,
                93.655563507, 88.930876023, 88.90080903, 86.788380294, 80.158093389, 79.54394481, 78.692859927,
                77.250164211, 75.564632781, 71.039905299, 59.334895917, 58.791100302, 57.503519073, 46.659403593,
                45.590090535, 44.118157743, 42.72802677, 41.394980088, 41.012738316, 38.970392571, 35.307938853, 35.183471301};

        List<Peak> expect_gcms_peaksList_Normalized = new ArrayList<>();
        List<Peak> expect_gcms_peaksList = new ArrayList<>();

        for (int i = 0; i < expect_mz.length; i++) {
            Peak expect_gcms_peaks = new Peak();
            expect_gcms_peaks.setMzValue(expect_mz[i]);
            expect_gcms_peaks.setIntensity(expect_intensity[i]);
            expect_gcms_peaksList.add(expect_gcms_peaks);
        }

        expect_gcms_peaksList_Normalized = normalizedPeakList(expect_gcms_peaksList);
        Spectrum expect_gcmsSpectrum = new Spectrum();
        expect_gcmsSpectrum.setSpectrum(expect_gcms_peaksList_Normalized);
        expect_gcmsSpectrum.setSpectrumId(43);

        assertEquals(expect_gcmsSpectrum, spectrumDB);
    }

    @Test
    void testInsertSpectraDBId75(){
        String sql = "SELECT p.mz, p.intensity FROM compound_identifiers as i " +
                "INNER JOIN compounds as c ON i.compound_id = c.compound_id " +
                "INNER JOIN gcms_spectrum as s ON c.compound_id = s.compound_id " +
                "INNER JOIN gcms_peaks as p ON s.gcms_spectrum_id = p.gcms_spectrum_id " +
                "WHERE i.inchi = :testinchi";

        int spectrumId = 75; //The original list (txt) id is 77
        String testinchi = "InChI=1S/C5H8O3/c1-3-4(6)5(7)8-2/h3H2,1-2H3";
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("testinchi", testinchi);
        Spectrum spectrumDB = jdbcTemplate.query(sql, param, spectrumPeaksExtractor(spectrumId));

        double expect_mz[] = {57.0329, 87.0439, 116.0463, 115.0383, 56.0255, 114.0310, 73.0284,
                58.0366, 59.0127, 74.0359, 91.0390, 42.0097, 117.0470, 117.0508, 88.0470, 116.0669,
                117.0174, 55.0166, 87.0680, 116.0871, 89.0236, 115.0731, 75.0419, 83.0469, 73.0650,
                95.0830, 133.0501};
        double expect_intensity[] = {9999, 2226.097368, 2121.9967791, 797.22286974, 453.314664,
                376.81411482, 302.95800117, 293.04729234, 195.7464234, 185.65223292, 146.0643921,
                125.80311843, 99.909068094, 95.74792425, 94.728436209, 93.456693396, 87.263132814,
                77.730556167, 74.73702555, 53.55034443, 48.097309788, 42.716297943, 40.248814716,
                32.324197257, 29.399329773, 29.353154391, 28.522417473};

        List<Peak> expect_gcms_peaksList_Normalized = new ArrayList<>();
        List<Peak> expect_gcms_peaksList = new ArrayList<>();

        for (int i = 0; i < expect_mz.length; i++) {
            Peak expect_gcms_peaks = new Peak();
            expect_gcms_peaks.setMzValue(expect_mz[i]);
            expect_gcms_peaks.setIntensity(expect_intensity[i]);
            expect_gcms_peaksList.add(expect_gcms_peaks);
        }

        expect_gcms_peaksList_Normalized = normalizedPeakList(expect_gcms_peaksList);
        Spectrum expect_gcmsSpectrum = new Spectrum();
        expect_gcmsSpectrum.setSpectrum(expect_gcms_peaksList_Normalized);
        expect_gcmsSpectrum.setSpectrumId(75);

        assertEquals(expect_gcmsSpectrum, spectrumDB);
    }

    private ResultSetExtractor<Spectrum> spectrumPeaksExtractor(int spectrumId) {
        return rs -> {
            Spectrum spectrum = new Spectrum();
            while (rs.next()) {
                if(spectrum.getSpectrumId() == -1) {
                    spectrum = //spectrumMap.computeIfAbsent(spectrumId, id ->
                            Spectrum.builder()
                                    .spectrumId(spectrumId)
                                    .spectrum(new ArrayList<>())
                                    .build();
                }
                Peak peak = Peak.builder()
                        .mzValue(rs.getDouble("mz"))
                        .intensity(rs.getDouble("intensity"))
                        .build();
                spectrum.getSpectrum().add(peak);
            }
            return spectrum;
        };
    }

    private static List<Peak> normalizedPeakList(List<Peak> gcms_peaksList){
        List<Peak> normalizedSpectrum = new ArrayList<>();
        int size = gcms_peaksList.size();
        for(int i=0; i<size; i++){
            //THE FIRST INTENSITY IS THE HIGHEST SINCE THE INTENSITIES ARE IN ORDER
            Peak gcms_peaks = new Peak();
            double mz = gcms_peaksList.get(i).getMzValue();

            double maxIntensity = gcms_peaksList.get(0).getIntensity();
            double intensity = gcms_peaksList.get(i).getIntensity();

            double normalizedIntensity = (intensity/maxIntensity)*100;

            gcms_peaks.setMzValue(mz);
            gcms_peaks.setIntensity(normalizedIntensity);
            normalizedSpectrum.add(gcms_peaks);
        }
        return normalizedSpectrum;
    }

}
