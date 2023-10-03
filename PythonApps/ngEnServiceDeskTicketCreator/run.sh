#!/bin/sh

cd /home/udm/PROD/NgEnServiceDeskTicketCreator/

currentDateTime="$(date \-u +'%Y%m%d'\ "%H:%M:%S")"

python3.8 main_app.py --mode="daily" --run_date="$currentDateTime"
