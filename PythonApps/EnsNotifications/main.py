# Third party
from datetime import datetime
import time
# Custom libs
from modules.config import config_object
from modules.generic_functions import update_regex_excel
from modules.logger import main_logger
from modules.notice_processor import parser_wrapper
from objects.exchange_url import ExchangeUrl
from modules.run_arguments import get_run_arguments
from modules import run_args as ar
from modules.url_config import url_configuration


def set_run_arguments():
    input_arguments = get_run_arguments()
    ar.exchanges = input_arguments['exchanges']
    ar.create_jiras = input_arguments['create_jiras']

    if not ar.create_jiras:
        main_logger.info('No jiras will be created in this run!')


def get_exchange_list(args_exchanges):
    exchange_dict = {
        'valid': [],
        'invalid': []
    }

    for each_exchange in args_exchanges:
        if each_exchange.strip() not in [exchange.strip() for exchange in config_object['PROCESSING']['supported_urls'].split(',')]:
            exchange_dict['invalid'].append(each_exchange.strip())
        else:
            exchange_dict['valid'].append(each_exchange.strip())

    return exchange_dict


def main():
    try:
        update_regex_excel()
    except FileNotFoundError:
        main_logger.debug(f'{config_object["SHAREPOINT"]["credential_file_path"]} not found. Regex Excel not updated.')
    set_run_arguments()
    exchange_urls = get_exchange_list(ar.exchanges)

    if exchange_urls['invalid']:
        main_logger.warning(f'The following specified exchanges are invalid: {", ".join(exchange_urls["invalid"])}')

    main_logger.info('--------------------------------------------------------------------------')

    for each_exchange in exchange_urls['valid']:
        datasource = url_configuration[each_exchange]['exchangeSourceName']
        for each_try in range(int(config_object['PROCESSING']['retries_number'])):
            try:
                main_logger.info(f'Start gathering notices for {datasource}')
                exchange = ExchangeUrl(each_exchange)
                main_logger.info(f'Start scraping {datasource}')
                parser_wrapper(exchange)
                main_logger.info(f'Finished scraping {datasource}')
                break
            except Exception as exception:
                if each_try < int(config_object['PROCESSING']['retries_number']) - 1:
                    main_logger.info(f'{datasource} retry number {each_try + 1}, due to failure: {exception.__str__()}\n\n')
                    time.sleep(5)
                    continue
                else:
                    main_logger.fatal(exception, exc_info=True)
                    main_logger.fatal(f'Fatal error when scraping {datasource}\n\n')
                    pass
    main_logger.info('--------------------------------------------------------------------------\nEnded processing.')


if __name__ == '__main__':
    main_logger.info('Starting application')

    run_start_time = datetime.now()
    main_logger.info(f'Start processing at {run_start_time}.')

    try:
        main()
    except Exception as e:
        main_logger.fatal(e, exc_info=True)
    run_end_time = datetime.now()
    main_logger.info(f'Ended processing at {run_end_time}. Finished in {run_end_time - run_start_time}')
