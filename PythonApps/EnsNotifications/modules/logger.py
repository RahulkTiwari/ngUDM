# Third party libs
import logging
from datetime import datetime
import os
from pathlib import Path
# Custom libs
from modules.config import config_object

log_level = config_object['LOGGING']['level']
file_name_root = config_object['LOGGING']['root_filename']
log_dir = config_object['LOGGING']['log_dir']

try:
    Path(log_dir).mkdir(parents=True, exist_ok=False)
except FileExistsError:
    pass

# Initialize main logger
main_logger = logging.getLogger(f'{file_name_root}')
main_logger.setLevel(getattr(logging, log_level))
time_now = datetime.now().strftime('%Y-%m-%d %H:%M:%S').replace(' ', '').replace(':', '')
main_logger_filehandler = logging.FileHandler(f'{log_dir}/{file_name_root}{str(time_now)}.log', encoding='utf-8')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
main_logger_filehandler.setFormatter(formatter)
console_handler = logging.StreamHandler()
console_handler.setLevel(getattr(logging, log_level))
main_logger.addHandler(main_logger_filehandler)
main_logger.addHandler(console_handler)

main_logger.info(f'Logger has been initialized for process id {os.getpid()}')
