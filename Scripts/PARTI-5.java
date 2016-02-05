/*
***************
* PART I-5
***************
*/

/**
* 
* 02/04/2016, David Bour
* 
* Looks for consensus sequences that were made and renames them without the 
* whitespace 2.
* 
*
* Rationale:
* This is done so it can match with the probable positive script further down 
* the workflow which looks at original names and not with the whitespace 2
* when trying to find sequences that made a consensus. The whitespace 2 is 
* added after Geneious saves post-trim on each sequence.
*
* Since the post trim sequences are used in assembly, it will append the whitespace 2.
* We could have renamed all of the post-trim beforehand BUT it will ruin the last
* script (PART III) which converts these post-trim back into their original names
* before being exported into the data table. If we did this earlier, the last step
* would not be able to discern between the two original names of the original raw vs post-trim.
*/


public static List<AnnotatedPluginDocument> performOperation(List<AnnotatedPluginDocument> documents, Options options,
														ProgressListener progressListener) throws DocumentOperationException
{

	// Gets the folder the files are in
	GeneiousService service = ServiceUtilities.getSelectedService();
	DatabaseService databaseService = (DatabaseService) service;

	// Grab all files in the document viewer
	List<AnnotatedPluginDocument> documentsInFolder = databaseService.retrieve("");

	for (AnnotatedPluginDocument docs : documentsInFolder)
	{
		String docString = docs.getDocumentClass().toString();
		if (docString.contains("DefaultAlignmentDocument")) 
		{
			String currName = (docs.getName());
			String newName = renameDupe(currName);
			docs.setName(newName);
			docs.save();
		}
	}


	return results;

}

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