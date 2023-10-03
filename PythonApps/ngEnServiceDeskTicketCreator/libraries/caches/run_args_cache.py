# Third party libraries
import argparse
# Custom libraries
from libraries.objects.RunArguments import RunArguments
from libraries.caches.config import config_object
from libraries.service_desk_logger import Logger


def read_arguments():
    determine_run_arguments = RunArguments()
    # Reading run arguments
    parser = argparse.ArgumentParser()
    parser.add_argument('--run_date',
                        help='Provide the date in which the run is executed (YYYYMMDD HH:MM:SS format)',
                        required=True)
    parser.add_argument('--mode',
                        help='Provide the execution mode: init (mode for one off initialization), daily (mode for daily executions)',
                        choices=['init', 'daily'],
                        required=True)
    parser.add_argument('--hour_offset',
                        help='Provide number of hours to offset from the run datetime to query for new notifications inserted (based on insDate) '
                             'and/or updated (based on updDate) since the last run. If omitted, default value used the one defined in config.ini.'
                             'If a high value is used, processing time will be higher but output will be the same.',
                        required=False)
    parser.add_argument('--day_offset',
                        help='Provide number of whole days to offset from the run date to query for new notifications inserted '
                             '(based on eventInsertDate) since the last run. If omitted, default value used the one defined in config.ini. '
                             'If a high value is used, processing time will be higher but output will be the same.',
                        required=False)

    args = parser.parse_args()

    determine_run_arguments.date = args.run_date
    determine_run_arguments.mode = args.mode
    determine_run_arguments.hours_offset = int(args.hour_offset) if args.hour_offset is not None else \
        int(config_object['OFFSET_DEFAULT']['HOUR_OFFSET'])
    determine_run_arguments.days_offset = int(args.day_offset) if args.day_offset is not None else int(config_object['OFFSET_DEFAULT']['DAY_OFFSET'])

    Logger.logger.info(f"Run with date {determine_run_arguments.date} and mode {determine_run_arguments.mode}.")

    return determine_run_arguments


run_arguments = read_arguments()
