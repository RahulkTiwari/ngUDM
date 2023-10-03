#!/bin/bash -x
cd /home/udm/
source QAairflowsandbox/bin/activate
cd /home/udm/PROD/PythonApps/EnsNotifications
python3 main.py --exchanges="all_codes"
