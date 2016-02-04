
/**
* 
* 02/04/2016, David Bour
* 
* Looks for sequences that passed trim AND has a partner that also passed
* the trimming criteria. If one of the pair is missing, exclude both
* in the workflow until reaching the custom script to output them as probable 
* positives.
*
* Rationale:
* This is done so the workflow does not crash for not being able to
* find a second pair when combining pairs by name for De Novo Assembly
* because one partner was removed by trim filtering. 
*/

