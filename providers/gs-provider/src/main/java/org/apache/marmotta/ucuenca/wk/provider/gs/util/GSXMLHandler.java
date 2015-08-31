/** 
 * ***********************************************
 * File		   - GSXMLHandler.java
 * Description - Google Scholar SAX Parser
 * 				 Called within GSsearchMainActivity
 * Author      - A. Arun Goud (DiodeDroid)
 * Date        - 2011/12/14 (First created)
 * 				 2012/01/15 (v1.0 released)
 * 				 2012/03/02 (v1.1 released)
 * 				 2012/05/09 (v2.0 released)
 *				 2012/12/18 (v2.1 released)
 * email	   - microbuff@hotmail.com
 * ***********************************************
 */
package org.apache.marmotta.ucuenca.wk.provider.gs.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//import android.util.Log;

public class GSXMLHandler extends DefaultHandler {

	static ArrayList<GSresult> gsresultlist = new ArrayList<GSresult>();
	GSresult gsresult = new GSresult();
	StringBuilder builder = new StringBuilder();
	private static String currenttext = null;
	private static int pageresultcount = 0;
	private static Pattern resulttag;
	private static Matcher resultmatch;
	private static Pattern citestag;
	private static Matcher citesmatch;
	private static int numresults = 0;
	private static int level = 0;

	/** The following variables act as flags to keep track of state during parsing */
	private static final int INGSR = 1;
	private static final int INPDF = 2;
	private static final int INCHECKPDF = 3;
	private static final int OUTCHECKPDF = 4;
	private static final int OUTPDF = 5; 
	private static final int INGSRI = 6;
	private static final int INGSRT = 7;
	private static final int INCHECKBOOK = 8;
	private static final int OUTCHECKBOOK = 9;
	private static final int INFULLTEXT = 10;
	private static final int OUTFULLTEXT = 10;
	//private static final int INLINK = 10;
	private static final int INTITLE = 11;
	private static final int OUTTITLE = 12;
	//private static final int OUTLINK = 13;
	private static final int OUTGSRT = 14;

	//static int inBOOK = 3;


	private static final int INAUTHOR = 15;
	private static final int OUTAUTHOR = 16;
	private static final int INTEXT = 17;
	private static final int OUTTEXT = 18;
	private static final int INCITE = 19;
	private static final int OUTCITE = 20;
	private static final int OUTGSRI = 21;	
	private static final int OUTGSR = 0;

	//private static final int TYPE = 1;
	private static final int ONLYLINK = 1;
	private static final int FREEPDF = 2;
	private static final int BOOK = 3;
	private static final int BOOKANDPDF = 4;
	private boolean isfreepdf = false;
	private boolean isbook = false;
	//private boolean D = false;

	//public static GSresultList gsresultlist = null;
	//private static final String TAG = "MyActivity";

	/** ArrayList that contains numresults number of results */
	public ArrayList<GSresult> getGSresultList() {
		return gsresultlist;
	}

	public void clearGSresultList() {
		gsresultlist.clear();
	}

	/** Number of results returned at the end of parsing. Usually 10. */
	public int getNumResults() {
		return numresults;
	}

	/** The parsing flow is as below. Terms in parentheses may not always appear during parsing. 
	 * inGSR->(inPDF->check if PDF/HTML link (incheckPDF->outcheckPDF)->outPDF)->
	 * inGSRI->inGSRT->(check if [BOOK] appears->)inLINK->inTITLE->outTITLE->outLINK->outGSRT->
	 * inAUTHOR->outAUTHOR->inTEXT->outTEXT->(inCITE->outCITE)->outGSRI->outGSR */

	/** State setting begins once "<div>","<span>","<a>", etc appear in the XML */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if ("div".equals(localName)&&(attributes.getLength()>=1)) {

			currenttext = attributes.getValue(0);
			
                        if ("gs_r".equalsIgnoreCase(currenttext)) {
				level = INGSR;
				pageresultcount++;
				//if(D) Log.d(TAG,"Found");
			}
                        else if ((level==INGSR)&&("gs_md_wp".equalsIgnoreCase(currenttext))) {
				level = INPDF;
				//if(D) Log.d(TAG,"pdf=yes");
			} //#3
			else if (((level==INGSR)||(level==OUTPDF))&&("gs_ri".equalsIgnoreCase(currenttext))) {
				level = INGSRI;
			} //#3
			else if ((level==OUTGSRT)&&("gs_a".equalsIgnoreCase(currenttext))) {
				level = INAUTHOR;
				builder.setLength(0);
			} //#3
			else if ((level==OUTAUTHOR)&&("gs_rs".equalsIgnoreCase(currenttext))) {
				level = INTEXT;
				builder.setLength(0);
			} //#3
			else if ((level==OUTTEXT)&&("gs_fl".equalsIgnoreCase(currenttext))) {
				level = INCITE;
				builder.setLength(0);

			} //#3
			else if ((level==OUTCHECKPDF)&&("gs_br".equalsIgnoreCase(currenttext))) {
				level = INFULLTEXT;

			} //#3

//                        if (currenttext.equalsIgnoreCase("gs_r")) {
//				level = INGSR;
//				pageresultcount++;
//				//if(D) Log.d(TAG,"Found");
//			} //#3
			// Previously "gs_ggs gs_fl"
//			else if ((level==INGSR)&&(currenttext.equalsIgnoreCase("gs_md_wp"))) {
//				level = INPDF;
//				//if(D) Log.d(TAG,"pdf=yes");
//			} //#3
//			else if (((level==INGSR)||(level==OUTPDF))&&(currenttext.equalsIgnoreCase("gs_ri"))) {
//				level = INGSRI;
//			} //#3
//			else if ((level==OUTGSRT)&&(currenttext.equalsIgnoreCase("gs_a"))) {
//				level = INAUTHOR;
//				builder.setLength(0);
//			} //#3
//			else if ((level==OUTAUTHOR)&&currenttext.equalsIgnoreCase("gs_rs")) {
//				level = INTEXT;
//				builder.setLength(0);
//			} //#3
//			else if ((level==OUTTEXT)&&currenttext.equalsIgnoreCase("gs_fl")) {
//				level = INCITE;
//				builder.setLength(0);
//
//			} //#3
//			else if ((level==OUTCHECKPDF)&&currenttext.equalsIgnoreCase("gs_br")) {
//				level = INFULLTEXT;
//
//			} //#3

			/** Get total number of results that Google Scholar displays on upper right corner. */
			if (pageresultcount==1) {
				/** New Google Scholar shows it on upper left */
				resulttag = Pattern.compile("[\\w]*\\s?([0-9,]+) results",Pattern.MULTILINE);			
				//if(D) Log.d(TAG,"\nString="+builder.toString());
				resultmatch = resulttag.matcher(builder.toString());
				if (resultmatch.find()) {
					NumberFormat format = NumberFormat.getInstance(Locale.US);          
					try {
						//if(D) Log.d(TAG,"Match="+resultmatch.group(1).toString());
						numresults = format.parse(resultmatch.group(1).replaceAll("About", "").trim()).intValue();
						//if(D) Log.d(TAG,"Match="+numresults);
					} catch (ParseException e) {
						//e.printStackTrace();
					} 

				}
			}

		}
		else if ("h3".equals(localName)&&(level==INGSRI)) {
			currenttext = attributes.getValue("class");			
			if("gs_rt".equalsIgnoreCase(currenttext)) {
				level = INGSRT;
                        }
		} // #3
//
//                else if (localName.equals("h3")&&(level==INGSRI)) {
//			currenttext = attributes.getValue("class");			
//			if(currenttext.equalsIgnoreCase("gs_rt"))
//				level = INGSRT;
//		} // #3

		else if ("a".equals(localName)) {
			if (level==INPDF) {
				//if(D) Log.d(TAG,"pdf="+attributes.getValue("href"));
				gsresult.setPDF(attributes.getValue("href"));
			} // #3
			else if (level==INFULLTEXT) {
				//if(D) Log.d(TAG,"pdf="+attributes.getValue("href"));
				gsresult.setFullTextLink(attributes.getValue("href"));
				level = OUTFULLTEXT;
			} // #3
			else if ((level==INGSRT)||(level==OUTCHECKBOOK)) {
				//if(D) Log.d(TAG,"link="+attributes.getValue("href"));
				gsresult.setLink(attributes.getValue("href"));
				builder.setLength(0);
				level = INTITLE;
				if((!isfreepdf)&&(!isbook)){
					gsresult.setType(ONLYLINK);
                                }

			} // #3

		}
		else if ("span".equals(localName)&&(attributes.getLength()>=1)) {
			currenttext = attributes.getValue("class");

			if ((level == INPDF) && "gs_ctg2".equalsIgnoreCase(currenttext)) {
				level = INCHECKPDF;
				builder.setLength(0);

			} //#3
			else if ((level == INGSRT) && "gs_ctc".equalsIgnoreCase(currenttext)) {
				level = INCHECKBOOK;
				builder.setLength(0);

			} //#3

		} 

	}

	/** State resetting once once end of "<div>","<span>","<a>", etc appear in the XML */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if ("div".equalsIgnoreCase(localName)) {
			// Only "Full text" appears
			if (level==INPDF) {
				level = OUTPDF;
			} // #3
			else if ((level== OUTCHECKPDF)||(level==OUTFULLTEXT)) {
				level = OUTPDF;
			} // #3
			else if (level==INAUTHOR) {
				//if(D) Log.d(TAG,"author="+builder.toString());
				gsresult.setAuthor(builder.toString());

				level = OUTAUTHOR;
			} //#3
			else if (level==INTEXT) {
				//if(D) Log.d(TAG,"text="+builder.toString());
				gsresult.setText(builder.toString().replaceAll("\n", " "));
				builder.setLength(0);
				level = OUTTEXT;
			} //#3
			else if (level==INCITE) {
				level = OUTCITE;
				citestag = Pattern.compile("Cited by (.*?) .*?",Pattern.MULTILINE);
				citesmatch = citestag.matcher(builder.toString());
				if (citesmatch.find()) {          
					try {						
						//if(D) Log.d(TAG,"Cites="+citesmatch.group(1));
						gsresult.setCites("Cited by - "+citesmatch.group(1));
					} 
					catch (Exception e) {
						//e.printStackTrace();
					} 

				}
                                else{
					gsresult.setCites("Cited by - NA");
                                }
			} //#3
			else if (level==OUTCITE) {
				level = OUTGSRI;
				//if(D) Log.d(TAG,"type="+gsresult.getType());

			} //#3
			else if (level==OUTGSRI) {
				level = OUTGSR;
				gsresultlist.add(new GSresult(gsresult));
				pageresultcount = 0;
				isfreepdf = false;
				isbook = false;
			} //#3
			else if (level==OUTTEXT) {
				level = OUTGSR;
				gsresult.setCites("Cited by - NA");
			} //#3


		}
		else if ("h3".equalsIgnoreCase(localName)) {
			if(level==OUTTITLE) {
				level = OUTGSRT;
			}
		} //#3
		else if ("a".equalsIgnoreCase(localName)) {
			if (level==INTITLE) {
				level = OUTTITLE;
				//if(D) Log.d(TAG,"title="+builder.toString());
				gsresult.setTitle(builder.toString());

			} //#3
		}
		else if ("span".equalsIgnoreCase(localName)) {
			if(level==INPDF) {
				level = OUTCHECKPDF;
			} //#3
			else if (level==INCHECKPDF) {
				//if(D) Log.d("MyApp","Type="+builder.toString());
				if (builder.toString().equalsIgnoreCase("[PDF]")) { 
					isfreepdf = true;
					gsresult.setType(FREEPDF);
				}
				level = OUTCHECKPDF;
			} //#3
			else if (level==INCHECKBOOK) {
				if ("[BOOK]".equalsIgnoreCase(builder.toString())) { 
					isbook = true;
					if (isfreepdf){
						gsresult.setType(BOOKANDPDF);
                                        }
                                        else {
						gsresult.setType(BOOK);	
                                        }
					//if(D) Log.d("MyApp","isPDF="+gsresult.getType());
				}
				level = OUTCHECKBOOK;
			} //#3
		} 


	}

	/** Save data that appears between relevant start and end tags in the XML */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		currenttext = new String(ch, start, length);
		builder.append(currenttext);

	}

}
