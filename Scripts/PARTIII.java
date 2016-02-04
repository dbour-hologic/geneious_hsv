// The following script was written to export Geneious documents after the workflow has completed. 
// Written by David Bour, 06/17/2015 for HSV validation.

/**
*************
*  PART III
*************
*/

// Version 8.26.2015 Changes
/* Changed the output format
* -- Output now returns 4 outputs
* ---- The outputs consists of a BLAST summary | REPORT summary | Raw AB1 summary | CONCENSUS summary
* ---- This was done for easier parsing with the LIMS system
*/

// Version 9.14.2015 Changes
/* Added functions (renameDupe & parseDupe)
* Functions created in response to saving the POST-TRIM length of individual sequences
*/

// Version 12.22.2015 Changes
/*
* Added feature to create a BLAST file that contained sequences that passed the
trim threshold, but did not produce a consensus sequence; probable positives.
*/

// Version 01.12.2016 Changes
/*
*
* Created error messages wrapped with try/catch blocks for each file export.
* Goal was to create empty files if no data is present and prompt an error message to see what went wrong.
*/

import java.io.*;
import java.text.*;
import java.util.regex.*;
import javax.swing.JOptionPane;

/** Used for naming the duplicate (".ab1 2") to its original file name
* @param seqDupeName name of the .ab1 file
* @return renamed to the original .ab1 file, removing the " 2"
*/
public static String renameDupe(String seqDupeName) {

	String renameIt = seqDupeName;
	String regPattern = "(.*\\.ab1)(\\s\\d)";
	Pattern p = Pattern.compile(regPattern);
	Matcher m = p.matcher(renameIt);
	
	if (m.matches()) {
		
		if (m.group(2).matches(" 2")) {
			return m.group(1);	
		}
	}
	return "";
}


/** Used for capturing the duplicate .ab1 file created from the "save" function
* This was necessary since we cannot get TRIM length without making a duplicate of the .ab1 file
* .ab1 after save function becomes .ab1 2 - We want to pass the .ab1 2 and not the .ab1 to the final write process
* @param seqDocName name of the .ab1 file
* @return true if pattern matches for *.ab1 2
*/
public static boolean parseDupe(String seqDocName) {

	String readNames = seqDocName;
	String seqName = renameDupe(readNames);

	if (!seqName.isEmpty()) {
		return true;	
	}
	return false;
	
}


public static List<AnnotatedPluginDocument> performOperation(List<AnnotatedPluginDocument> documents, Options options,
													  ProgressListener progressListener) throws DocumentOperationException, IOException 
{
	
	// The following is used for naming purposes.
	
	Calendar theCurrentTime = new GregorianCalendar();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");	
	String currentTimeString = sdf.format(theCurrentTime.getTime());

	
	// This is where Geneious API goes to work.
	
	// Gets the current working folder/directory on the left hand side of GUI.
	GeneiousService service = ServiceUtilities.getSelectedService();
	
	if (service instanceof DatabaseService) 
	
		{
			
		// Sets up for query.
		DatabaseService databaseService = (DatabaseService) service;
		
		// Get all documents in the document table and store into a list obj. 
		List<AnnotatedPluginDocument> documentInFolder = databaseService.retrieve("");
					
		if (documentInFolder.size() == 0) throw new DocumentOperationException("No documents in folder");

		try {
		
		// Convert the list into an array.
		AnnotatedPluginDocument[] documentsToExport = documentInFolder.toArray(new AnnotatedPluginDocument[documentInFolder.size()]);
		
		// API that does the exporting, the directory should be changed as needed.
		
		// RegEx to parse Plate number out
		String regPattern = ".*-(P\\d+)-\\D.*";
		String plate = "PlateUnknown";
		
		// Search through the document list for a file with "Plate##" in the name or else default to "PlateUnknown"
		for (int count = 0; count < documentsToExport.length; count++)
		{
			String getDocName = documentsToExport[count].getName();
			Pattern p = Pattern.compile(regPattern);
			Matcher m = p.matcher(getDocName);
			if (m.matches())
			{
				plate = m.group(1); 
				break;
			} 
		}
		
		// Create ArrayList to parse separate out different file-types
		ArrayList<AnnotatedPluginDocument> reportArray = new ArrayList<AnnotatedPluginDocument>();
		ArrayList<AnnotatedPluginDocument> conArray = new ArrayList<AnnotatedPluginDocument>();
		ArrayList<AnnotatedPluginDocument> seqArray = new ArrayList<AnnotatedPluginDocument>();
		ArrayList<AnnotatedPluginDocument> blastArray = new ArrayList<AnnotatedPluginDocument>();

		ArrayList<AnnotatedPluginDocument> probableArray = new ArrayList<AnnotatedPluginDocument>();
		
		// Go through documents after the analysis is done and sort into their respective arrays
		for (int x = 0; x < documentsToExport.length; x++)
		{
			String queryResult = documentsToExport[x].getDocumentClass().toString();
			
			if (queryResult.contains("DefaultNucleotideGraphSequence")) 
			{
				
				System.out.println(documentsToExport[x]);
				
				if (parseDupe(documentsToExport[x].getName())) {
				
					String newName = renameDupe(documentsToExport[x].getName());
					System.out.println(newName);	
					documentsToExport[x].setName(newName);
					documentsToExport[x].save();
					seqArray.add(documentsToExport[x]);
				}
				
			} 
			else if (queryResult.contains("DefaultAlignmentDocument")) 
			{
				conArray.add(documentsToExport[x]);
				//System.out.println(documentsToExport[x]);
			} 
			else if (queryResult.contains("NucleotideBlastSummaryDocument"))
			{

				String nameOfDoc = documentsToExport[x].getFieldValue("query").toString();
				if (!nameOfDoc.contains("ReadsConsensus"))
				{
				probableArray.add(documentsToExport[x]);
				}
				else
				{
				blastArray.add(documentsToExport[x]);
				}

				//System.out.println(documentsToExport[x]);
			} 
			else 
			{
				reportArray.add(documentsToExport[x]);
				//System.out.println(documentsToExport[x]);
			}
			
		}
		
		// Convert the ArrayList to Array for the exporter method
		AnnotatedPluginDocument[] report = new AnnotatedPluginDocument[reportArray.size()];
		AnnotatedPluginDocument[] consensus = new AnnotatedPluginDocument[conArray.size()];
		AnnotatedPluginDocument[] blast = new AnnotatedPluginDocument[blastArray.size()];
		AnnotatedPluginDocument[] rawseq =  new AnnotatedPluginDocument[seqArray.size()];

		AnnotatedPluginDocument[] prob = new AnnotatedPluginDocument[probableArray.size()];
		
		report = reportArray.toArray(report);
		consensus = conArray.toArray(consensus);
		blast = blastArray.toArray(blast);
		rawseq = seqArray.toArray(rawseq);

		prob = probableArray.toArray(prob);

		String fileName = "HSV_RUN-" + plate + ".csv";
		
		

		// Citrix Testing
		
		PluginUtilities.exportDocuments(new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-rawseq-" + fileName), rawseq);

			try 
			{
				PluginUtilities.exportDocuments(new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-probableBLAST-" + fileName), prob);
			} 
			catch (UnsupportedOperationException pe)
			{
				File probFile = new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-probableBLAST-" + fileName);
				probFile.createNewFile();
				JOptionPane.showMessageDialog(null, "No probable positives found. \n Click 'OK' to continue.", "NOTICE", JOptionPane.WARNING_MESSAGE);
			} 

			try
			{
				PluginUtilities.exportDocuments(new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-consensus-" + fileName), consensus);
			}
			catch (UnsupportedOperationException ce)
			{
				File conFile = new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-consensus-" + fileName);
				conFile.createNewFile();
				JOptionPane.showMessageDialog(null, "No consensus sequences were made. \n Click 'OK' to continue.", "NOTICE", JOptionPane.WARNING_MESSAGE);
			}

			try
			{
				PluginUtilities.exportDocuments(new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-blast-" + fileName), blast);
			}
			catch (UnsupportedOperationException be)
			{
				File blastFile = new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-blast-" + fileName);
				blastFile.createNewFile();
				JOptionPane.showMessageDialog(null, "No BLAST results were found. \n Click 'OK' to continue.", "NOTICE", JOptionPane.WARNING_MESSAGE);
				
			}

			try
			{
				PluginUtilities.exportDocuments(new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-summary-" + fileName), report);
			}
			catch (UnsupportedOperationException summ)
			{
				File sumFile = new File("T:////Geneious/QAS/geneious_output/" + currentTimeString + "-summary-" + fileName);
				sumFile.createNewFile();
				summ.printStackTrace();
				
			}

		
		} catch (IOException e) {
			throw new DocumentOperationException(e);
		} 
			
		}
		
	return Collections.emptyList();
}