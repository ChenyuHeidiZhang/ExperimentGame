# ExperimentGame

App developed for a multi-attribute decision making experiment.

June 21, 2020

Current Trial info file description:

Column 1-4: outcome (win / lose / no outcome) for each option of the trial

Column 5: horizontal (0) / vertical (1)

Column 6: task type
* 1 = 2Opt2Att
* 2 = 2Optt4Att
* 3 = 4Opt2Att
* 4 = 4Opt4Att

Column 7 is trial type: 1 = non-dominated, 0 = dominated

Columns 8:end are the attribute names (A+1, P+1, A-1, P-1, A+2, P+2, etc) and corresponding magnitudes

There are 5 Sessions total
* Each session is comprised of 20 blocks (5 blocks for each task type); Each block is comprised of 40 trials (32 non-dominated, 8 dominated)


June 8, 2020

The app is refactored to read spatial configuration from the updated format of csv file. Attribute values for each trial are no longer recorded in the sqlite database; only the trial number (row number in the csv file, starting from 1) is recorded and relevant information can be obtained from the csv.

TODO: Erik would need to merge this with the event codes sent via Bluetooths. Do we want to change the event codes (which is not implemented here)? As we take into account different spatial configurations, do we send code by spatial location or attribute type? Right now the program can be easily modified to send by spatial location. 


May 30, 2020

Notes:

To enable bluetooth on a tablet, please go to settings and connect to the bluetooth device. Then in the app, tap on the BLUETOOTH button.

~~Options with 4 attributes now have two spatial configurations (randomized)~~
~~- amount win, prob win, amount lose, prob lose (recorded as config 0 in database)~~
~~- amount win, amount lose, prob win, prob lose (recorded as config 1 in database)~~


May 21, 2020

Notes:

1. To change the percentage (chance) of trials getting counted towards the total:

Go to ResultActivity line 26, change the **PERCENT_WIN** variable.

2. To change the password:

Go to res/values/strings.xml line 11

3. Task (/trial) information is given in a **.csv file named "allTrialsMADM_Tablet.csv."**

**To replace this file, go to ExperimentGame/app/src/main/assets, copy the new csv file into the folder. (Need to remove the first line (header) of the file.)**


------------------------------

May 11, 2020

This version of the app completes the implementation of 4 configurations of tasks.

TODO:

1. Uncomment the code for bluetooth. Write event codes. Test this.

2. Sync sqlite timestamps table to Firebase.


-----------
Mar 21, 2020

Up to this point, each trial in the experiment only consists of 2 choices each containing 2 attributes (probability and amount).

The current plan is to extend it to include trials with 4 choices and choices with 4 attributes (probability of winning, winning amount, probability of losing, losing amount).

