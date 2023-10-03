# Third party libraries
import datetime
import logging
from pathlib import Path
# Custom libraries
from libraries.caches.config import config_object

LOG_LEVELS = {
    50: 'CRITICAL',
    40: 'ERROR',
    30: 'WARNING',
    20: 'INFO',
    10: 'DEBUG',
    0: 'NOTSET'
}


class Logger:

    log_config = config_object['LOG']

    log_dir = log_config['log_file_path']

    try:
        Path(log_dir).mkdir(parents=True, exist_ok=False)
    except FileExistsError:
        pass

    log_date = (str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')).replace(" ", "")).replace(":", "")
    log_file = log_dir + "/log_" + log_date + ".log"
    logger = logging.getLogger(__name__)
    handler = logging.FileHandler(log_file)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.addHandler(logging.StreamHandler())
    logger.setLevel(getattr(logging, log_config['log_level'].upper()))
    logger.info(f'Logger has been initialized with level {LOG_LEVELS[logger.getEffectiveLevel()]}.')
    logger.info(f'Logs will be written to {log_file}')
