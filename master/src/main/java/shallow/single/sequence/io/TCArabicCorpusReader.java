package shallow.single.sequence.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.io.TCReaderSingleLabel;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

//this class reads the corpus, will be used by Demo class
public class TCArabicCorpusReader 
  extends JCasCollectionReader_ImplBase implements TCReaderSingleLabel {
	
	public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	 public static final String PARAM_LANGUAGE_CODE = "LanguageCode";
	 @ConfigurationParameter(name = PARAM_LANGUAGE_CODE, mandatory = true)
	 protected String language;
	 
	/**
	 * Path to the file containing the corpus = text to be segmented
	 */

	public static final String PARAM_SOURCE_FOLDER = "SourceFolder";
	@ConfigurationParameter(name = PARAM_SOURCE_FOLDER, mandatory = true)
	private String sourceFolder;
	
	/*public static final String PARAM_SOURCE_LOCATION = "SourceLocation";
	@ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
	private String sourceLocation;*/
	
	 public static final String PARAM_INPUT_FILE = "Corpus";
	 @ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = false)
	 protected File corpus;
	
	 public static final String PARAM_GOLD_FILE = "GoldFile";
	 @ConfigurationParameter(name = PARAM_GOLD_FILE, mandatory = false)
	 protected File goldFile;
	
	public static final String PARAM_PATTERNS = ComponentParameters.PARAM_PATTERNS;
	@ConfigurationParameter(name = PARAM_PATTERNS, mandatory = false)
	private String[] patterns;
	
	private int fileOffset;
	private List<String> sentences;
	private List<String> label_golds;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		
		  super.initialize(context);
          
		  String mergedFileUri = sourceFolder + "/" + corpus;

			  System.out.println( "Target file: " + mergedFileUri);		  
			  System.out.println( "Source Folder: " + sourceFolder);
			  
		
	    fileOffset = 0;
		sentences = new ArrayList<String>();
		label_golds = new  ArrayList<String>();

		try {

			 for (String sentence : FileUtils.readLines(corpus, encoding)) {
				if (sentence.trim().length() > 0) {
					sentences.add(sentence);
					System.out.println(" corpus sentence is " + " " + sentence);
				}
			}
			 
			 for (String labels_sentence : FileUtils.readLines(goldFile, encoding)) {
				 if (labels_sentence.trim().length() > 0) {
				 String[] line = labels_sentence.split(" ");
					for (String label: line) {
						label_golds.add(label);
			            System.out.println(" label is " + " " + label);
			         }
				 }
			 }
     
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new ResourceInitializationException(e);
		}
	}
	
	
	public Progress[] getProgress() {
		System.out.println("the fileOffSet is : " + " " + fileOffset);
		System.out.println("the progress is : " + " " + Progress.ENTITIES);
		 return new Progress[] { new ProgressImpl(fileOffset, sentences.size(), Progress.ENTITIES) };
	}
	
	public boolean hasNext() throws IOException, CollectionException {
		System.out.println("has next");
		return fileOffset < sentences.size() && fileOffset < label_golds.size();
	}
	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		
// 1.set metadata (as we are creating more than one CAS out of a single file,
//we need to have different document titles and URIs for each CAS,
//otherwise serialized CASes will be overwritten),
		DocumentMetaData dmd = DocumentMetaData.create(jCas);
		dmd.setDocumentTitle(dmd.getDocumentTitle() + "-" + fileOffset);
		dmd.setDocumentUri(dmd.getDocumentUri() + "-" + fileOffset);
		dmd.setDocumentId(String.valueOf(fileOffset));
		dmd.setLanguage("ar");
			           
//	  2. set document text (a sentence in this case), or should it be tokens?
//		String sentence = jCas.getDocumentText();
		String sentence = sentences.get(fileOffset);
	    jCas.setDocumentText(sentence);
	    jCas.setDocumentLanguage("ar");
	    
//	    set outcome (the actual value, labels)
		 TextClassificationOutcome outcome = new TextClassificationOutcome(jCas);
	        outcome.setOutcome(getTextClassificationOutcome(jCas));
	        outcome.addToIndexes();
	        
	        fileOffset++;
	        
	}

	
	public String getTextClassificationOutcome(JCas jCas) {
		return label_golds.get(fileOffset).toString();
	}
	
	 public String getSentences() throws TextClassificationException
	    {
	        return sentences.get(fileOffset);
	    }
	
}


