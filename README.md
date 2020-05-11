# ExperimentGame

App developed for a multi-attribute decision making experiment.

May 11, 2020

This version of the app completes the implementation of 4 configurations of tasks.

Task (/trial) information is given in a .csv file named "allTrialsMADM_Tablet.csv."
Description of this file:
Column 1: task type
	◦	1 = 2Opt2Att
	◦	2 = 2Optt4Att
	◦	3 = 4Opt2Att
	◦	4 = 4Opt4Att
Column 2: trial type
	◦	1 = non-dominated
	◦	0 = dominated
Columns 3:end are the magnitudes for each option type according to the same mappings as AllTOD
There are 5 Sessions total
	◦	Each session is comprised of 20 blocks (5 blocks for each task type, 800 trials in each block); Each block is comprised of 40 trials (32 non-dominated, 8 dominated)

* To replace this file, go the assets folder, copy the new csv file into the folder, and remove the first line (header) of the file.

-----------
Mar 21, 2020

Up to this point, each trial in the experiment only consists of 2 choices each containing 2 attributes (probability and amount).

The current plan is to extend it to include trials with 4 choices and choices with 4 attributes (probability of winning, winning amount, probability of losing, losing amount).

