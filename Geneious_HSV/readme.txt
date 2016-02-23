############################
READ ME

The following repository contains
1) Custom Java code for Geneious version 8.1.2 (http://www.geneious.com)
2) Workflow Files compatible with only Geneious (XML format)

The custom Java code is to be implemented within the Geneious system itself and
is not a stand alone, so compiling won't do any good. Geneious runs these on its
automated workflow when you place them into the "Custom Java" workflow box.

To get this to run, these custom scripts must be placed in order with PART I
being before PART II and PART III being after PART I and PART II. They are not
necesarrily placed one after another but in between in-built workflow modules
within the "custom workflow" management system. These PART names can be found in
the header of all of the script files.

The workflow should be built as follows:
(g) signifies built-in Geneious modules
(c) custom Java code

1(g) - TRIM ENDS
2(g) - SAVE DOCUMENTS/BRANCH
3(c) - PART I script
4(g) - ALIGN/ASSEMBLE -> DE NOVO ASSEMBLE
5(g) - FILTER DOCUMENTS
6(g) - SAVE DOCUMENTS/BRANCH
7(g) - BLAST
8(g) - SAVE DOCUMENTS/BRANCH
9(g) - COMBINE WITH EARLIER DOCUMENTS (starting from 4)
10(g) - FILTER DOCUMENTS - parameters (Each document description Does not contain "Assembly" + Name Contains "consensus")
11(g) - SAVE DOCUMENTS/BRANCH
12(c) - PART II script
13(g) - BLAST
14(g) - SAVE DOCUMENTS/BRANCH
15(c) - PART III script

To get a better idea of the parameters, upload the workflow itself into Geneious. There's a "hack" that allows
you to use old scripts on newer versions of Geneious BUT its highly discouraged as the API may have changed since version
8.1.2 in which was this built for.

Hack (At your own risk!!):
Open the Geneious Workflow file (*.geneiousWorkflow) in a text editor and change the following on line 2

geneiousVersion="8.1.2" to geneiousVersion="<Version Number Currently Using>"

Upload this workflow into Geneious
