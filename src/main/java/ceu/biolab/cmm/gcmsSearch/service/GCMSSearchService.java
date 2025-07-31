package ceu.biolab.cmm.gcmsSearch.service;

import ceu.biolab.cmm.gcmsSearch.domain.*;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSFeatureQueryDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSQueryResponseDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchResponseDTO;
import ceu.biolab.cmm.gcmsSearch.repository.GCMSSearchRepository;

import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
//@SuperBuilder
public class GCMSSearchService {

    @Autowired
    private GCMSSearchRepository gcmsSearchRepository;

    public GCMSSearchResponseDTO search(GCMSSearchRequestDTO request) {

        //int nSpectrum = request.getGcmsSpectrum().size(); //NUMBER OF GCMS SPECTRUM

        Spectrum gcmsSpectrumExperimental = request.getGcmsSpectrumExperimental();

        ColumnType columnType = request.getColumnType();
        DerivatizationMethod derivatizationMethod = request.getDerivatizationMethod();
        double RI = request.getRetentionIndex();
        //double RITolerance = request.getRetentionIndexTolerance()*0.01; //Data is %
        double RITolerance = request.getRetentionIndexTolerance();
        double RIToleranceFinal;

        if (RITolerance >= 0 && RITolerance <= 100){//
            RIToleranceFinal = RITolerance*0.01;
        } else {
            throw new IllegalArgumentException("Retentention Index Tolerance must be positive(%)" +request.getRetentionIndexTolerance());
        }

        GCMSSearchResponseDTO response = new GCMSSearchResponseDTO();

        /*Spectrum spectrum = new Spectrum();
        //ITERATES OVER THE SPECTRA
        for (int i = 0; i < nSpectrum; i++) {
            //NUMBER OF PEAKS PER SPECTRUM
            int nPeaksSpectrum = request.getGcmsSpectrum().get(i).getSpectrum().size();
            //ITERATES OVER THE PEAKS OF THE SPECTRUM i
            for(int j=0; j<nPeaksSpectrum; j++){
                spectrum = request.getGcmsSpectrum().get(i); //Spectrum i
                List<Peak> peakList = spectrum.getSpectrum(); //Peak list of my spectrum
                Peak peak = spectrum.getSpectrum().get(j);
                double mz = spectrum.getSpectrum().get(j).getMzValue();
                double intensity = spectrum.getSpectrum().get(j).getIntensity();
            }
        }*/

        /*IMFeature feature = new IMFeature(mz, ccs);
        AnnotatedFeature imAnnotatedFeature = new AnnotatedFeature(feature);*/
        //GCMSFeature gcmsFeature = new GCMSFeature(feature);

        double RIDifference = RI * RIToleranceFinal;
        double RILower = RI - RIDifference;
        double RIUpper = RI + RIDifference;

        //GCMSFeatureQueryDTO queryData = new GCMSFeatureQueryDTO(RILower, RIUpper, derivatizationMethod, columnType);
        //USE OF SUPERBUILDER
        GCMSFeatureQueryDTO queryData = GCMSFeatureQueryDTO.builder().minRI(RILower).maxRI(RIUpper)
                .derivatizationMethod(derivatizationMethod).columnType(columnType).build();

        try {
            //TODO CAMBIAR EN FUNCION DE LAS NUEAS QUERYS - hecho
            //THE COMPOUNDS ARE ALREADY MERGED
            List<GCMSQueryResponseDTO> queryResults = gcmsSearchRepository.findMatchingCompounds(queryData);

            List<GCMSAnnotation> gcmsAnnotationList = new ArrayList<>();


            //TODO -> pasar queryresult a gcmsannotation y de ahi pasarlo a gcmsfeature
            //ITERATES OVER THE RESULTS OF THE QUERY
            for (GCMSQueryResponseDTO queryResult : queryResults) {
                //TODO AÑADIR TODA INFO COMPOUNDS -> hecho
                GCMSCompound gcmsCompound = GCMSCompound.builder()
                        .compoundId(queryResult.getCompoundId())
                        .compoundName(queryResult.getCompoundName())
                        .mass(queryResult.getMonoisotopicMass())
                        .formula(queryResult.getFormula())
                        .formulaType(queryResult.getFormulaType())
                        .logP(queryResult.getLogP())
                        .dbRI(queryResult.getRI())
                        //.dbRT(queryResult.getRT())
                        .derivatizationMethod(queryResult.getDertype())
                        .gcColumn(queryResult.getGcColumn())
                        .GCMSSpectrum(queryResult.getGCMSSpectrum())
                        .build();

                double dbRI = queryResult.getRI();
                //RI by user (experimental) - RI database -> absolute value so that it is positive
                double deltaRI = Math.abs(RI - dbRI);
                GCMSAnnotation gcmsAnnotation = GCMSAnnotation.builder().gcmsCompound(gcmsCompound)
                        .experimentalRI(RI).deltaRI(deltaRI)
                        .build();
                // Recibe el espectro experimental para calcular el cosine Score
                gcmsAnnotation.cosineScoreFunction(gcmsSpectrumExperimental);

                gcmsAnnotationList.add(gcmsAnnotation);

                //*imCompound.addPathway(pathway); //imcompound es gcmscompoundAll //Cambiarlo a gcmscompound
                //Annotation annotation = new Annotation(imCompound); //no necesito esta
                //annotations.add(annotation); //no la necesito //lista de gcmsannotaciones
                //AnnotationsByAdduct annotationsByAdduct = new AnnotationsByAdduct(adduct, annotations); //gcmsannotation//no necesito
                 //*
            }
            GCMSFeature gcmsFeature = GCMSFeature.builder().gcmsAnnotations(gcmsAnnotationList).build();

            response.addGcmsFeatures(gcmsFeature);

            //imAnnotatedFeature.addAnnotationByAdduct(annotationsByAdduct); //GCMSFeature
            //response.addImFeature(imAnnotatedFeature);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

}
