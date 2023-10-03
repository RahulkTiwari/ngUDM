# Third party libs
from datetime import datetime
import time
import sys
# Custom libs
from modules.logger import main_logger, log_traceback
from modules.config import config_object
from modules.fetch_data import fetch_data
from modules.run_args_parser import read_arguments
from modules import run_arg_cache as rac


def get_exchange_list(mic_code_list):
    mic_code_dict = {
        'valid': [],
        'invalid': []
    }

    for each_exchange in mic_code_list:
        if each_exchange not in [exchange.strip() for exchange in config_object['PROCESSING']['supported_codes'].split(',')]:
            mic_code_dict['invalid'].append(each_exchange.strip())
        else:
            mic_code_dict['valid'].append(each_exchange.strip())

    return mic_code_dict


def main():
    """
    Main function of the application to get the exchanges from the input argument and iterate over them to get the short sell data from
    the exchanges' websites.
    :return: None
    """

    run_start_time = datetime.now()
    main_logger.info(f'Starting application at {run_start_time}')

    read_arguments(sys.argv[1:])

    scrape_mic_list = get_exchange_list(rac.source_codes)

    if scrape_mic_list['invalid']:
        main_logger.warning(f'Invalid source code(s) provided: {", ".join(scrape_mic_list["invalid"])}')

    main_logger.info(f'Processing source codes {", ".join(scrape_mic_list["valid"])}')
    main_logger.info(f'Output file format is: {rac.output_format}')
    main_logger.info(f'{"-"*75}\n')

    number_of_retries = int(config_object['PROCESSING']['retries_number'])

    final_return = 0

    for each_code in scrape_mic_list['valid']:
        for each_try in range(number_of_retries):
            try:
                fetch_return = fetch_data(each_code)
                if fetch_return:
                    final_return = 1
                    main_logger.error(f'Finished processing with errors for : {each_code}')
                break
            except Exception as ex:
                main_logger.info(f'Retrying for: {each_code}')
                if each_try < number_of_retries - 1:
                    log_traceback(ex, each_code)
                    time.sleep(5)
                    continue
                else:
                    final_return = 1
                    log_traceback(ex, each_code, level='fatal')
                    pass

    run_end_time = datetime.now()

    main_logger.info(f'{"-" * 75}\n')
    main_logger.info(f'Ended processing at {run_end_time}. Finished in {run_end_time - run_start_time}')

    exit(final_return)


if __name__ == '__main__':

    try:
        main()
    except Exception as e:
        main_logger.fatal(e, exc_info=True)
