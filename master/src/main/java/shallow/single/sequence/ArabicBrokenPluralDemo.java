package shallow.single.sequence;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.weka.WekaAdapter;
import org.dkpro.tc.ml.weka.report.WekaOutcomeIDReport;
import org.dkpro.tc.features.maxnormalization.TokenRatioPerDocument;
import org.dkpro.tc.features.ngram.WordNGram;
import org.dkpro.tc.ml.builder.MLBackend;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import shallow.single.sequence.annotators.BreakIteratorSegmenter;
import shallow.single.sequence.io.TCArabicCorpusReader;
import shallow.single.sequence.annotators.SequenceOutcomeAnnotator;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import de.tudarmstadt.ukp.dkpro.core.api.resources.DkproContext;

public class ArabicBrokenPluralDemo 
      implements Constants  {
	
	public static int SAMPLE_SIZE = 1371045;
	public static int NUM_FOLDS = 10;
	public static final String LANGUAGE_CODE = "ar";
	
	public static final String sourceFolder = "src/main/resources/data/corpora";
	public static final String goldSourceFolder = "src/main/resources/data";
	public static final String labeledDataSet = "src/main/resources/data/labeled.txt";
	public static final String corpus = "src/main/resources/data/corpora/jazz.txt";
	public static String SOURCE_FOLDER =  "C:/MasterProject/work-space/DKPRO_HOME";
	
	 public static void main(String[] args) throws Exception
	    {
		 
		 String baseDir = DkproContext.getContext().getWorkspace().getAbsolutePath();
		 SOURCE_FOLDER = baseDir;
		 System.setProperty("DKPRO_HOME", "C:/MasterProject/work-space/DKPRO_HOME");
		 
//		 if ( args != null && args.length >= 2){
//	    		
//	        	if ( args[0] != null && args[0].trim().length() != 0 ){
//	        		String sizeinfo = args[0];
//	        		SAMPLE_SIZE = Integer.parseInt(sizeinfo.trim());
//	        	}
//	        		
//	        	if ( args[1] != null && args[1].trim().length() != 0 ){
//	        		String foldInfo = args[1];
//	        		NUM_FOLDS = Integer.parseInt(foldInfo.trim());
//	        	}
//
//	    	}
//		 System.out.println("args SAMPLE_SIZE:" + SAMPLE_SIZE); 
		 
		 ArabicBrokenPluralDemo  demo = new ArabicBrokenPluralDemo ();
		 demo.runCrossValidation(getParameterSpace()); 
	    }
		 
		 public static ParameterSpace getParameterSpace() throws ResourceInitializationException
		    {
			 
		  // configure training data reader dimension
                Map<String, Object> dimReaders = new HashMap<String, Object>();

		        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
		        		TCArabicCorpusReader.class,
		        		TCArabicCorpusReader.PARAM_SOURCE_FOLDER, sourceFolder,
		        		TCArabicCorpusReader.PARAM_INPUT_FILE, corpus,
		        		TCArabicCorpusReader.PARAM_PATTERNS, "[+]*.txt",
		        		TCArabicCorpusReader.PARAM_LANGUAGE_CODE, LANGUAGE_CODE);
		        dimReaders.put(DIM_READER_TRAIN, readerTrain);
		        
		        /*CollectionReaderDescription readerLabel = CollectionReaderFactory.createReaderDescription(
		        		TCArabicCorpusReader.class,
		        		TCArabicCorpusReader.PARAM_SOURCE_FOLDER, goldSourceFolder,
		        		TCArabicCorpusReader.PARAM_PATTERNS, "[+]*.txt",
		        		TCArabicCorpusReader.PARAM_LANGUAGE_CODE, LANGUAGE_CODE);
		        dimReaders.put(DIM_READER_TRAIN, readerLabel);*/
   
			ParameterSpace pSpace = new ParameterSpace(
		        Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_FEATURE_MODE, Constants.FM_SEQUENCE),
                Dimension.create(DIM_LEARNING_MODE, Constants.LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_SET,
                    new TcFeatureSet(
                      TcFeatureFactory.create(TokenRatioPerDocument.class),
                      TcFeatureFactory.create(WordNGram.class, WordNGram.PARAM_NGRAM_USE_TOP_K,
                              600, WordNGram.PARAM_NGRAM_MIN_N, 1, WordNGram.PARAM_NGRAM_MAX_N,6)),
                Dimension.create(DIM_CLASSIFICATION_ARGS,
                		new MLBackend(new WekaAdapter(), NaiveBayes.class.getName()),
                        new MLBackend(new WekaAdapter(), RandomForest.class.getName())
             )));
                return pSpace;
   
		    }
	 
	 public void runCrossValidation(ParameterSpace pSpace) throws Exception
	    {
		 System.out.println("args NUM_FOLDS:" + NUM_FOLDS);
		 ExperimentCrossValidation  CVExp = new ExperimentCrossValidation();
		    CVExp.setExperimentName("firstexperement");
		    CVExp.setType("evaluation");
		    CVExp.setNumFolds(NUM_FOLDS);
		    CVExp.addReport(WekaOutcomeIDReport.class);
	        CVExp.setParameterSpace(getParameterSpace()); 
	        Lab.getInstance().run(CVExp);
	    }
	    
	    protected AnalysisEngineDescription getPreprocessing() throws ResourceInitializationException 
	    {
	        return 
	         createEngineDescription
	         ((createEngineDescription(BreakIteratorSegmenter.class,
	        		 BreakIteratorSegmenter.PARAM_LANGUAGE, "ar")),
	         createEngineDescription(SequenceOutcomeAnnotator.class));
	    }
	    
	   
	   
}
