package com.roef.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

/**
 *
 * @author kareem
 */
public class SentenceAnalyzer {

	public static final String NLP_MODELS_PATH = //"C:\\Users\\kareem\\Desktop\\Master\\JARS and Resources\\nlp-models\\";
			"nlp_models\\";

	public static void findName() {
		try {
			InputStream is = new FileInputStream(NLP_MODELS_PATH + "en-ner-person.bin");

			TokenNameFinderModel model = new TokenNameFinderModel(is);
			is.close();

			NameFinderME nameFinder = new NameFinderME(model);

			String[] sentence = new String[]{
					"The",
					"Mike",
					"Smith",
					"is",
					"a",
					"good",
					"person"
			};
			Span nameSpans[] = nameFinder.find(sentence);
			for (Span s : nameSpans) {
				System.out.println(s.getType());
				System.out.println(s.toString());
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void SentenceDetect() {
		try {
			String paragraph = "nlp_models\\";

			// always start with a model, a model is learned from training data
			InputStream is
			= new FileInputStream(NLP_MODELS_PATH + "da-sent.bin");
			SentenceModel model = new SentenceModel(is);
			SentenceDetectorME sdetector = new SentenceDetectorME(model);

			String sentences[] = sdetector.sentDetect(paragraph);

			System.out.println(sentences[0]);
			System.out.println(sentences[1]);
			is.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void Tokenize() {
		try {
			InputStream is = new FileInputStream(NLP_MODELS_PATH + "en-token.bin");
			TokenizerModel model = new TokenizerModel(is);
			Tokenizer tokenizer = new TokenizerME(model);
			String tokens[] = tokenizer.tokenize("where i can find restaurant");
			for (String a : tokens) {
				System.out.println(a);
			}
			is.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static ArrayList<String> POSTag(String input) {

		ArrayList<String> queryVocabs = new ArrayList<String>();
		try {
			POSModel model = new POSModelLoader().load(new File(NLP_MODELS_PATH + "en-pos-maxent.bin"));
			PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
			POSTaggerME tagger = new POSTaggerME(model);

			ObjectStream<String> lineStream = new PlainTextByLineStream(
					new StringReader(input));
			perfMon.start();
			String line;
			while ((line = lineStream.read()) != null) {
				String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);
				String[] tags = tagger.tag(whitespaceTokenizerLine);
				POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
				System.out.println("===" + sample.toString().split(" ").length);
				String queryVocab[] = sample.toString().split(" ");
				for (int i = 0; i < queryVocab.length; ++i) {
					queryVocabs.add(queryVocab[i]);
				}
				System.out.println(sample.toString());
				perfMon.incrementCounter();
			}

			perfMon.stopAndPrintFinalResult();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return queryVocabs;
	}

	public static void chunk() {
		try {
			//            I_PRP 
			//            /want_VBP 
			//            to_TO 
			//            eat_VB
			//            then_RB 
			//            I_PRP
			//            want_VBP
			//            to_TO 
			//            watch_VB
			//            movie_NN

			POSModel model = new POSModelLoader().load(new File(NLP_MODELS_PATH + "en-pos-maxent.bin"));
			PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
			POSTaggerME tagger = new POSTaggerME(model);
			//            String input = "Hi. How are you? this done broke is Mike.";
			String input = "i want to eat";
			ObjectStream<String> lineStream = new PlainTextByLineStream(
					new StringReader(input));
			perfMon.start();
			String line;
			String whitespaceTokenizerLine[] = null;
			String[] tags = null;
			while ((line = lineStream.read()) != null) {
				whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE.tokenize(line);
				tags = tagger.tag(whitespaceTokenizerLine);

				POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
				System.out.println(sample.toString());
				perfMon.incrementCounter();
			}
			perfMon.stopAndPrintFinalResult();
			// chunker
			InputStream is = new FileInputStream(NLP_MODELS_PATH + "en-chunker.bin");
			ChunkerModel cModel = new ChunkerModel(is);
			ChunkerME chunkerME = new ChunkerME(cModel);
			String result[] = chunkerME.chunk(whitespaceTokenizerLine, tags);
			for (String s : result) {
				System.out.println(s);
			}
			Span[] span = chunkerME.chunkAsSpans(whitespaceTokenizerLine, tags);
			for (Span s : span) {
				System.out.println(s.toString());
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void Parse() {
		try {
			// http://sourceforge.net/apps/mediawiki/opennlp/index.php?title=Parser#Training_Tool
			InputStream is = new FileInputStream(NLP_MODELS_PATH + "en-parser-chunking.bin");
			ParserModel model = new ParserModel(is);
			Parser parser = ParserFactory.create(model);
			String sentence = "Programcreek is a very huge and useful website.";
			Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);
			for (Parse p : topParses) {
				p.show();
			}
			is.close();
			/*
			 * (TOP (S (NP (NN Programcreek) ) (VP (VBZ is) (NP (DT a) (ADJP (RB
			 * very) (JJ huge) (CC and) (JJ useful) ) ) ) (. website.) ) )
			 */        } catch (Exception exception) {
				 exception.printStackTrace();
			 }
	}

	public static void main(String[] args) {
		//        SentenceDetector.SentenceDetect();
		//        SentenceDetector.findName();
		SentenceAnalyzer.POSTag("I want to eat  then I want to watch movie ");
		//        SentenceDetector.chunk();
		//        SentenceDetector.Parse();
		//        SentenceDetector.Tokenize();

	}
}
