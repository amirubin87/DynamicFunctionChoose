#!/bin/bash 
logName=LOGS/ML.log
for commsSize in "s/ONp10" "b/ONp10" "b-avg-40/ONp50" "s-avg-40/ONp50";
do
	for om in `seq 2 8`;
	do		   
		for i in `seq 1 10`;
		do
			graphID=$commsSize$om$i
			echo '			run on /N5000$commsSize/OM$om/$i/'
			echo '			run on /N5000$commsSize/OM$om/$i/' >> $logName
			echo ''
			echo ''
			echo ''
			echo ''
			echo ''
						
			START=$(date +%s)
			
			TODO "../../benchmarks/binary_networks/N5000$commsSize/OM$om/$i/" "network.dat" "MLFeatures.csv" "true" "$graphID"
			
			END=$(date +%s)
			DIFF=$(( $END - $START ))
			echo "Took $DIFF seconds" >> $logName
		done
	   fi
	done
done
