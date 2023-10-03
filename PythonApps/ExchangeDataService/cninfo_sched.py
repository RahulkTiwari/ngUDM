# Third party libs
import time
# Custom libs
from exchange_parsers.cninfo import retrieve_data
from modules.logger import main_logger
from main import main


if __name__ == '__main__':
    main_logger.info('Starting scheduler for cninfo')

    retries = 45

    for each_try in range(retries):
        main_logger.debug(f'Attempt {each_try + 1} for retrieve data...')
        retrieved_data = retrieve_data()
        if retrieved_data.success:
            main_logger.info('Exchange data available, starting processing')
            main()
            main_logger.info('Exiting cninfo scheduler')
            exit(0)
        else:
            time.sleep(60)

    main_logger.fatal('Failed to retrieve data for cninfo')
    exit(1)
